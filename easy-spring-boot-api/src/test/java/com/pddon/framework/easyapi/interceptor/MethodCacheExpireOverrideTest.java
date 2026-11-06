package com.pddon.framework.easyapi.interceptor;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.pddon.framework.easyapi.properties.EasyApiConfig;

/**
 * 优化点4的验证：方法缓存TTL支持按key前缀配置覆盖。
 * 默认配置为空Map，完全沿用注解值（User:Perms/User:DataPerms仍为3600秒）；
 * 配置后覆盖值对缓存写入与清理同时生效，避免写入与删除落在不同TTL桶。
 */
public class MethodCacheExpireOverrideTest {

    private static final long ANNOTATION_PERMS_SECONDS = 3600L;

    @Test
    public void testDefaultConfigKeepsAnnotationValue() {
        MethodCacheInterceptorManager manager = newManager(new EasyApiConfig());
        Assert.assertEquals(ANNOTATION_PERMS_SECONDS, manager.resolveExpireSeconds("User:Perms", ANNOTATION_PERMS_SECONDS));
        Assert.assertEquals(180L, manager.resolveExpireSeconds("Other:Key", 180L));
        Assert.assertEquals(180L, manager.resolveExpireSeconds(null, 180L));
        Assert.assertEquals(180L, manager.resolveExpireSeconds("", 180L));
    }

    @Test
    public void testConfiguredPrefixOverridesOnlyThatPrefix() {
        EasyApiConfig config = new EasyApiConfig();
        config.getMethodCache().getExpireSecondsByPrefix().put("User:Perms", 600L);
        MethodCacheInterceptorManager manager = newManager(config);

        Assert.assertEquals(600L, manager.resolveExpireSeconds("User:Perms", ANNOTATION_PERMS_SECONDS));
        Assert.assertEquals(ANNOTATION_PERMS_SECONDS, manager.resolveExpireSeconds("User:DataPerms", ANNOTATION_PERMS_SECONDS));
    }

    @Test
    public void testInvalidConfiguredValueFallsBackToAnnotation() {
        EasyApiConfig config = new EasyApiConfig();
        config.getMethodCache().getExpireSecondsByPrefix().put("User:Perms", 0L);
        config.getMethodCache().getExpireSecondsByPrefix().put("User:DataPerms", -1L);
        MethodCacheInterceptorManager manager = newManager(config);

        Assert.assertEquals(ANNOTATION_PERMS_SECONDS, manager.resolveExpireSeconds("User:Perms", ANNOTATION_PERMS_SECONDS));
        Assert.assertEquals(ANNOTATION_PERMS_SECONDS, manager.resolveExpireSeconds("User:DataPerms", ANNOTATION_PERMS_SECONDS));
    }

    @Test
    public void testNullConfigKeepsAnnotationValue() {
        MethodCacheInterceptorManager manager = new MethodCacheInterceptorManager();
        Assert.assertEquals(ANNOTATION_PERMS_SECONDS, manager.resolveExpireSeconds("User:Perms", ANNOTATION_PERMS_SECONDS));
    }

    private MethodCacheInterceptorManager newManager(EasyApiConfig config) {
        MethodCacheInterceptorManager manager = new MethodCacheInterceptorManager();
        ReflectionTestUtils.setField(manager, "easyApiConfig", config);
        return manager;
    }
}
