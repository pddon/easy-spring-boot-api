package com.pddon.framework.easyapi.cache;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.cache.Cache;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.properties.LocalCacheConfig;

/**
 * 优化点3的验证：本地缓存的单桶容量上限与桶数量上限改为可配置。
 * 默认值与改造前硬编码值一致（100000/2000），配置后立即生效。
 */
public class LocalCacheContainerConfigTest {

    private static final Long LEGACY_MAX_CACHE_SIZE = 100000L;
    private static final Long LEGACY_MAX_CACHE_NUM = 2000L;

    @Test
    public void testDefaultsMatchLegacyHardcodedValues() {
        LocalCacheConfig config = new LocalCacheConfig();
        Assert.assertEquals(LEGACY_MAX_CACHE_SIZE, config.getMaxCacheSize());
        Assert.assertEquals(LEGACY_MAX_CACHE_NUM, config.getMaxCacheNum());

        LocalCacheContainer container = new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE);
        Assert.assertEquals(LEGACY_MAX_CACHE_SIZE, ReflectionTestUtils.getField(container, "maxCacheSize"));
        Assert.assertEquals(LEGACY_MAX_CACHE_NUM, ReflectionTestUtils.getField(container, "maxCacheNum"));
    }

    @Test
    public void testValuesResolvedFromConfiguration() {
        LocalCacheConfig config = new LocalCacheConfig();
        config.setMaxCacheSize(8L);
        config.setMaxCacheNum(3L);
        StaticListableBeanFactory factory = new StaticListableBeanFactory();
        factory.addBean("localCacheConfig", config);
        ObjectProvider<LocalCacheConfig> provider = factory.getBeanProvider(LocalCacheConfig.class);

        Assert.assertEquals(Long.valueOf(8L), LocalCacheContainer.resolveMaxCacheSize(provider));
        Assert.assertEquals(Long.valueOf(3L), LocalCacheContainer.resolveMaxCacheNum(provider));
    }

    @Test
    public void testInvalidConfiguredValuesFallBackToDefaults() {
        LocalCacheConfig config = new LocalCacheConfig();
        config.setMaxCacheSize(0L);
        config.setMaxCacheNum(-1L);
        LocalCacheContainer container = new LocalCacheContainer(
                CacheExpireMode.EXPIRE_AFTER_WRITE, false, config.getMaxCacheSize(), config.getMaxCacheNum());
        Assert.assertEquals(LEGACY_MAX_CACHE_SIZE, ReflectionTestUtils.getField(container, "maxCacheSize"));
        Assert.assertEquals(LEGACY_MAX_CACHE_NUM, ReflectionTestUtils.getField(container, "maxCacheNum"));
    }

    @Test
    public void testPerBucketMaximumSizeIsApplied() {
        LocalCacheContainer container = new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, false, 10L, 5L);
        for (int i = 0; i < 100; i++) {
            container.set("key-" + i, i, 77L);
        }
        Map<String, Cache<String, Object>> cacheMap = cacheMap(container);
        Assert.assertTrue("单桶容量应受配置约束", cacheMap.get("77").asMap().size() <= 10);
    }

    @Test
    public void testBucketCountLimitIsApplied() {
        LocalCacheContainer container = new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, false, 1000L, 5L);
        for (int i = 0; i < 50; i++) {
            container.set("key-" + i, i, 100L + i);
        }
        Map<String, Cache<String, Object>> cacheMap = cacheMap(container);
        Assert.assertTrue("桶数量应受配置约束，实际=" + cacheMap.size(), cacheMap.size() <= 5);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Cache<String, Object>> cacheMap(LocalCacheContainer container) {
        return (Map<String, Cache<String, Object>>) ReflectionTestUtils.getField(container, "cacheMap");
    }
}
