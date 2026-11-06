package com.pddon.framework.easyapi.cache;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.state.RedisStateStore;

/**
 * 真实Redis集成测试（默认跳过）。
 * <p>执行方式：mvn -o -pl easy-spring-boot-api test -Dtest=RedisIntegrationTest
 * -Dredis.test.enabled=true -Dredis.host=127.0.0.1 -Dredis.port=6379</p>
 * <p>未开启门控时使用JUnit Assume跳过，跳过原因会出现在测试报告中，不会伪装成通过。</p>
 */
public class RedisIntegrationTest {

    @Test
    public void testRedisBackedCacheAcrossInstances() {
        String enabled = System.getProperty("redis.test.enabled", System.getenv("REDIS_TEST_ENABLED"));
        Assume.assumeTrue("未开启真实Redis集成测试（需 -Dredis.test.enabled=true 或环境变量 REDIS_TEST_ENABLED=true），已跳过",
                Boolean.parseBoolean(enabled));

        String host = System.getProperty("redis.host", System.getenv("REDIS_HOST"));
        if (host == null || host.trim().isEmpty()) {
            host = "127.0.0.1";
        }
        String portValue = System.getProperty("redis.port", System.getenv("REDIS_PORT"));
        int port = portValue == null || portValue.trim().isEmpty() ? 6379 : Integer.parseInt(portValue.trim());
        String password = System.getProperty("redis.password", System.getenv("REDIS_PASSWORD"));

        LettuceConnectionFactory factory = new LettuceConnectionFactory(host, port);
        if (password != null && !password.isEmpty()) {
            factory.setPassword(password);
        }
        RedisStateStore storeA = null;
        try {
            factory.afterPropertiesSet();
            StringRedisTemplate template = new StringRedisTemplate(factory);
            template.afterPropertiesSet();

            String runPrefix = "easyapi:it:" + UUID.randomUUID().toString() + ":";
            storeA = new RedisStateStore(template, runPrefix);
            RedisStateStore storeB = new RedisStateStore(template, runPrefix);
            RedisStateCacheManager cacheManagerA = new RedisStateCacheManager(storeA);
            RedisStateCacheManager cacheManagerB = new RedisStateCacheManager(storeB);

            CacheManagerDistributedTest.CacheValue value = new CacheManagerDistributedTest.CacheValue();
            value.setName("redis");
            value.setCount(9);
            cacheManagerA.set("pojo", value, 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);

            CacheManagerDistributedTest.CacheValue loaded = cacheManagerB.get("pojo",
                    CacheManagerDistributedTest.CacheValue.class, 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);
            Assert.assertNotNull("实例B应通过Redis命中实例A写入的对象", loaded);
            Assert.assertEquals("redis", loaded.getName());
            Assert.assertEquals(9, loaded.getCount());

            String raw = template.opsForValue().get(runPrefix + "cache:write:pojo");
            Assert.assertNotNull("Redis中应存在带统一前缀、可读的key", raw);
            Assert.assertTrue("值必须是JSON而非JDK序列化", raw.startsWith("{"));

            cacheManagerB.setExpire("pojo", 120L, 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);
            Assert.assertNotNull(cacheManagerA.get("pojo", CacheManagerDistributedTest.CacheValue.class, 120L,
                    CacheExpireMode.EXPIRE_AFTER_WRITE));

            cacheManagerB.remove("pojo", 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);
            Assert.assertNull("实例B删除后实例A不应再命中", cacheManagerA.get("pojo",
                    CacheManagerDistributedTest.CacheValue.class, 60L, CacheExpireMode.EXPIRE_AFTER_WRITE));

            cacheManagerA.set("captcha", "1234", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE);
            Assert.assertEquals("1234", cacheManagerB.get("captcha", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE));
            cacheManagerB.remove("captcha", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE);
            Assert.assertNull(cacheManagerA.get("captcha", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE));

            cacheManagerA.clear();
            Assert.assertNull("clear()应通过SCAN按前缀清理cache命名空间",
                    template.opsForValue().get(runPrefix + "cache:write:pojo"));
        } finally {
            if (storeA != null) {
                try {
                    storeA.deleteByPrefix("");
                } catch (Exception ignored) {
                    // 清理失败不影响用例结论
                }
            }
            factory.destroy();
        }
    }
}
