package com.pddon.framework.easyapi.cache;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.dto.Session;
import com.pddon.framework.easyapi.impl.CacheableLanguageTranslateManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultCacheManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultSessionManagerImpl;
import com.pddon.framework.easyapi.properties.ErrorCodeProperties;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.state.LocalStateStore;

/**
 * 分布式CacheManager语义测试：两个"实例"（各自持有独立的CacheManager）共享同一状态存储，
 * 验证会话、方法缓存、国际化缓存、验证码的跨实例行为，以及本地模式对照。
 */
public class CacheManagerDistributedTest {

    private static final String KEY_PREFIX = "easyapi:test:";

    private LocalStateStore newSharedStore() {
        return new LocalStateStore(new LocalWriteCacheContainer(), KEY_PREFIX);
    }

    /**
     * 模拟一个应用实例：独立的CacheManager，底层共享同一store（等价于两个JVM连同一个Redis）
     */
    private RedisStateCacheManager newInstance(LocalStateStore sharedStore) {
        // 两个实例各自持有独立的缓存管理器对象，但共享底层状态存储
        return new RedisStateCacheManager(sharedStore);
    }

    @Test
    public void testValueStoredAsReadableJsonWithPrefix() {
        LocalStateStore store = newSharedStore();
        RedisStateCacheManager cacheManager = newInstance(store);
        cacheManager.set("biz:key", "value-1", 60L);

        String raw = store.get("cache:write:biz:key", 60L);
        Assert.assertNotNull("值应写入统一前缀下的cache命名空间", raw);
        Assert.assertTrue("值必须是JSON，禁止JDK序列化", raw.startsWith("{"));
        Assert.assertTrue("JSON中应包含原始类型信息", raw.contains("\"c\":\"java.lang.String\""));
        Assert.assertTrue("JSON中应包含值", raw.contains("\"v\":\"value-1\""));
    }

    @Test
    public void testMethodCacheHitAndEvictAcrossInstances() {
        LocalStateStore sharedStore = newSharedStore();
        RedisStateCacheManager cacheManagerA = newInstance(sharedStore);
        RedisStateCacheManager cacheManagerB = newInstance(sharedStore);

        MethodCacheManager methodCacheA = methodCacheManager(cacheManagerA);
        MethodCacheManager methodCacheB = methodCacheManager(cacheManagerB);

        String key = "DemoService:query:1";
        CacheValue value = new CacheValue();
        value.setName("demo");
        value.setCount(7);
        methodCacheA.cacheResult(key, value, null, 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);

        CacheValue fromB = (CacheValue) methodCacheB.getResult(key, CacheValue.class, null, 60L,
                CacheExpireMode.EXPIRE_AFTER_WRITE);
        Assert.assertNotNull("实例B应命中实例A写入的方法缓存", fromB);
        Assert.assertEquals("demo", fromB.getName());
        Assert.assertEquals(7, fromB.getCount());

        methodCacheB.remove(key, null, 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);
        Assert.assertNull("实例B失效后实例A也不应再命中", methodCacheA.getResult(key, CacheValue.class, null,
                60L, CacheExpireMode.EXPIRE_AFTER_WRITE));
    }

    private MethodCacheManager methodCacheManager(CacheManager cacheManager) {
        MethodCacheManager manager = new MethodCacheManager();
        ReflectionTestUtils.setField(manager, "cacheManager", cacheManager);
        ReflectionTestUtils.setField(manager, "systemParameterRenameProperties", new SystemParameterRenameProperties());
        return manager;
    }

    @Test
    public void testCacheReadableWhenTtlArgumentDiffers() {
        LocalStateStore sharedStore = newSharedStore();
        RedisStateCacheManager cacheManager = newInstance(sharedStore);
        cacheManager.set("ttl-key", "value", 60L);

        Assert.assertEquals("未指定时效的读取应能命中60秒写入的key", "value", cacheManager.get("ttl-key", String.class));
        Assert.assertTrue(cacheManager.exists("ttl-key"));
        cacheManager.remove("ttl-key");
        Assert.assertFalse("未指定时效的删除应能删除不同TTL写入的key", cacheManager.exists("ttl-key"));
    }

    @Test
    public void testSessionReadableAcrossInstances() {
        LocalStateStore sharedStore = newSharedStore();
        RedisStateCacheManager cacheManagerA = newInstance(sharedStore);
        RedisStateCacheManager cacheManagerB = newInstance(sharedStore);

        Session session = new Session();
        session.setSessionId("session-1").setUserId("user-1").setUsername("allen")
                .setLocale("zh_CN").setCreateTime(new Date());
        cacheManagerA.set("session-1", session, 15L, CacheExpireMode.EXPIRE_AFTER_REDA);

        DefaultSessionManagerImpl managerB = new DefaultSessionManagerImpl();
        managerB.setCacheManager(cacheManagerB);
        managerB.setExpireSeconds(15L);
        Session loaded = managerB.get("session-1");
        Assert.assertNotNull("实例B应能读取实例A创建的会话", loaded);
        Assert.assertEquals("user-1", loaded.getUserId());
        Assert.assertEquals("zh_CN", loaded.getLocale());
        Assert.assertTrue(managerB.exists("session-1"));

        managerB.refreshAliveTime("session-1");
        Assert.assertNotNull("续期后会话仍应存在", managerB.get("session-1"));

        managerB.remove("session-1");
        Assert.assertNull("实例B删除后实例A也不应再读到会话", cacheManagerA.get("session-1", Session.class, 15L,
                CacheExpireMode.EXPIRE_AFTER_REDA));
    }

    @Test
    public void testLanguageCacheSharedAcrossInstances() {
        LocalStateStore sharedStore = newSharedStore();
        RedisStateCacheManager cacheManagerA = newInstance(sharedStore);
        RedisStateCacheManager cacheManagerB = newInstance(sharedStore);

        CountingLanguageTranslateManager translateManager = new CountingLanguageTranslateManager();
        CacheableLanguageTranslateManagerImpl languageA = languageManager(cacheManagerA, translateManager);
        CacheableLanguageTranslateManagerImpl languageB = languageManager(cacheManagerB, translateManager);

        Assert.assertEquals("你好", languageA.get("welcome", "zh_CN"));
        Assert.assertEquals(1, translateManager.calls);
        Assert.assertEquals("实例B应命中实例A写入的国际化缓存", "你好", languageB.get("welcome", "zh_CN"));
        Assert.assertEquals("命中缓存后不应再次调用翻译器", 1, translateManager.calls);

        cacheManagerB.remove("zh_CN" + "welcome", 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);
        Assert.assertEquals("你好", languageB.get("welcome", "zh_CN"));
        Assert.assertEquals("缓存失效后应重新翻译", 2, translateManager.calls);
    }

    private CacheableLanguageTranslateManagerImpl languageManager(CacheManager cacheManager,
            CountingLanguageTranslateManager translateManager) {
        EasyApiConfig config = new EasyApiConfig();
        config.setEnableLanguageCache(true);
        config.setLanguageCacheSeconds(60L);
        config.setAlwaysUseDefaultLocale(false);

        CacheableLanguageTranslateManagerImpl manager = new CacheableLanguageTranslateManagerImpl();
        ReflectionTestUtils.setField(manager, "cacheManager", cacheManager);
        ReflectionTestUtils.setField(manager, "easyApiConfig", config);
        ReflectionTestUtils.setField(manager, "errorCodeProperties", new ErrorCodeProperties());
        ReflectionTestUtils.setField(manager, "languageTranslateManager", translateManager);
        return manager;
    }

    /**
     * 验证码（CaptchaServiceImpl）与邮箱码（EmailServiceImpl）都使用
     * CacheManager.get(key, expireSeconds, WRITE) + remove 的消费方式，这里按相同语义验证跨实例一次性消费。
     */
    @Test
    public void testCaptchaConsumedAcrossInstances() {
        LocalStateStore sharedStore = newSharedStore();
        RedisStateCacheManager cacheManagerA = newInstance(sharedStore);
        RedisStateCacheManager cacheManagerB = newInstance(sharedStore);

        cacheManagerA.set("captcha-1", "1234", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE);
        Assert.assertEquals("未指定类型的读取应还原字符串", "1234",
                cacheManagerA.get("captcha-1", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE));

        Assert.assertEquals("实例B应能读取实例A生成的验证码", "1234",
                cacheManagerB.get("captcha-1", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE));
        cacheManagerB.remove("captcha-1", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE);
        Assert.assertNull("验证码被实例B消费后，实例A不应再读到",
                cacheManagerA.get("captcha-1", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE));
        Assert.assertFalse(cacheManagerA.exists("captcha-1", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE));
    }

    @Test
    public void testLocalModeParityWithDefaultCacheManager() {
        LocalWriteCacheContainer writeContainer = new LocalWriteCacheContainer();
        LocalReadCacheContainer readContainer = new LocalReadCacheContainer();
        DefaultCacheManagerImpl cacheManager = new DefaultCacheManagerImpl();
        cacheManager.setLocalWriteCacheContainer(writeContainer);
        cacheManager.setLocalReadCacheContainer(readContainer);

        cacheManager.set("k60", "v60", 60L);
        cacheManager.set("k120", "v120", 120L);
        Assert.assertEquals("v60", cacheManager.get("k60", String.class, 60L));
        Assert.assertNull("本地实现按TTL分桶，60秒桶中不存在时不应命中", cacheManager.get("k60", String.class, 120L));

        cacheManager.clear(60L);
        Assert.assertNull("clear(60)应清理60秒桶", cacheManager.get("k60", String.class, 60L));
        Assert.assertEquals("clear(60)不应影响120秒桶", "v120", cacheManager.get("k120", String.class, 120L));

        cacheManager.clear();
        Assert.assertNull(cacheManager.get("k120", String.class, 120L));
    }

    public static class CacheValue {
        private String name;
        private int count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    /**
     * 计数翻译器：用于断言国际化缓存命中后不再触发真实翻译
     */
    private static class CountingLanguageTranslateManager implements com.pddon.framework.easyapi.LanguageTranslateManager {
        private int calls;
        private String locale = "zh_CN";

        @Override
        public String get(String key, String locale, Object... params) {
            calls++;
            return "zh_CN".equals(locale) ? "你好" : key;
        }

        @Override
        public String get(String key, Object... params) {
            return get(key, locale, params);
        }

        @Override
        public void setDefaultLocale(String locale) {
            this.locale = locale;
        }

        @Override
        public String getDefaultLocale() {
            return locale;
        }
    }
}
