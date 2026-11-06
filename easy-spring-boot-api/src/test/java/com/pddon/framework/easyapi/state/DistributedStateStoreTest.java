package com.pddon.framework.easyapi.state;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.redis.RedisConnectionFailureException;

import com.pddon.framework.easyapi.cache.LocalWriteCacheContainer;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.impl.DefaultCheckRepeatManagerImpl;
import com.pddon.framework.easyapi.properties.EasyApiConfig;

/**
 * 统一状态存储回归：
 * 本地实现的原子语义、TTL、前缀删除、跨实例唯一性（共享同一store模拟多实例），以及Redis故障降级。
 */
public class DistributedStateStoreTest {

    private LocalStateStore newLocalStore() {
        return new LocalStateStore(new LocalWriteCacheContainer(), "easyapi:test:");
    }

    private DefaultCheckRepeatManagerImpl repeatManager(DistributedStateStore store) {
        DefaultCheckRepeatManagerImpl manager = new DefaultCheckRepeatManagerImpl();
        manager.setEasyApiConfig(new EasyApiConfig());
        manager.setStateStore(store);
        return manager;
    }

    @Test
    public void testLocalStoreBasicSemantics() {
        LocalStateStore store = newLocalStore();
        Assert.assertTrue(store.setIfAbsent("k1", "v1", 60L));
        Assert.assertFalse("key已存在时不应再次占用", store.setIfAbsent("k1", "v2", 60L));
        Assert.assertEquals("v1", store.get("k1", 60L));
        store.set("k1", "v3", 60L);
        Assert.assertEquals("v3", store.get("k1", 60L));
        Assert.assertTrue(store.exists("k1", 60L));
        Assert.assertTrue(store.delete("k1", 60L));
        Assert.assertNull(store.get("k1", 60L));
    }

    @Test
    public void testLocalStoreTtlIsWriteHintNotReadSelector() {
        LocalStateStore store = newLocalStore();
        store.setIfAbsent("cross-ttl", "v", 60L);
        Assert.assertTrue("不同ttl参数不应影响key存在性判断", store.exists("cross-ttl", 120L));
        Assert.assertEquals("不同ttl参数不应影响读取", "v", store.get("cross-ttl", 120L));
        Assert.assertFalse("存在时不应被重复占用", store.setIfAbsent("cross-ttl", "v2", 120L));
        Assert.assertTrue(store.delete("cross-ttl", 120L));
        Assert.assertFalse(store.exists("cross-ttl", 60L));
    }

    @Test
    public void testLocalStoreTtlExpires() throws Exception {
        LocalStateStore store = newLocalStore();
        store.setIfAbsent("ttl-key", "v", 1L);
        Thread.sleep(1500L);
        Assert.assertNull("TTL到期后应自动失效", store.get("ttl-key", 1L));
        Assert.assertTrue("失效后应允许重新占用", store.setIfAbsent("ttl-key", "v", 1L));
    }

    @Test
    public void testIncrementIsAtomic() throws Exception {
        final LocalStateStore store = newLocalStore();
        final int threads = 16;
        final int loops = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            pool.execute(() -> {
                try {
                    start.await();
                    for (int j = 0; j < loops; j++) {
                        store.increment("counter", 1L, 60L);
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        Assert.assertTrue(done.await(30, TimeUnit.SECONDS));
        pool.shutdownNow();
        Assert.assertEquals((long) threads * loops, Long.parseLong(store.get("counter", 60L)));
    }

    @Test
    public void testDeleteByPrefix() {
        LocalStateStore store = newLocalStore();
        store.setIfAbsent("repeat:a", "1", 60L);
        store.setIfAbsent("repeat:b", "1", 60L);
        store.setIfAbsent("sign:nonce:a", "1", 60L);
        Assert.assertEquals(2L, store.deleteByPrefix("repeat:"));
        Assert.assertNull(store.get("repeat:a", 60L));
        Assert.assertNotNull(store.get("sign:nonce:a", 60L));
    }

    @Test
    public void testCrossInstanceRepeatCodeOnlyOnePasses() throws Exception {
        // 两个实例（各自持有独立的ReduceManager）共享同一个状态存储
        final LocalStateStore sharedStore = newLocalStore();
        final DefaultCheckRepeatManagerImpl instanceA = repeatManager(sharedStore);
        final DefaultCheckRepeatManagerImpl instanceB = repeatManager(sharedStore);
        final int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger passed = new AtomicInteger();
        AtomicInteger blocked = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            final DefaultCheckRepeatManagerImpl target = (i % 2 == 0) ? instanceA : instanceB;
            pool.execute(() -> {
                try {
                    start.await();
                    target.check("shared-token");
                    passed.incrementAndGet();
                } catch (BusinessException e) {
                    int code = e.getCode();
                    if (code == ErrorCodes.UNIQUE_TOKEN_USING.getCode()
                            || code == ErrorCodes.UNIQUE_TOKEN_USED.getCode()) {
                        blocked.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        Assert.assertTrue(done.await(30, TimeUnit.SECONDS));
        pool.shutdownNow();
        Assert.assertEquals("跨实例同一提交码只应成功一次", 1, passed.get());
        Assert.assertEquals(threads - 1, blocked.get());
    }

    @Test
    public void testReleaseOnOneInstanceAllowsAnotherToAcquire() {
        LocalStateStore sharedStore = newLocalStore();
        DefaultCheckRepeatManagerImpl instanceA = repeatManager(sharedStore);
        DefaultCheckRepeatManagerImpl instanceB = repeatManager(sharedStore);
        instanceA.check("retry-token");
        instanceA.used("retry-token");
        try {
            instanceB.check("retry-token");
            Assert.fail("成功后同码再次请求应被拒绝");
        } catch (BusinessException e) {
            Assert.assertEquals((long) ErrorCodes.UNIQUE_TOKEN_USED.getCode(), (long) e.getCode());
        }
        instanceB.release("retry-token");
        instanceB.check("retry-token");
        instanceB.used("retry-token");
        try {
            instanceA.check("retry-token");
            Assert.fail("已提交的码仍应被拒绝");
        } catch (BusinessException e) {
            Assert.assertEquals((long) ErrorCodes.UNIQUE_TOKEN_USED.getCode(), (long) e.getCode());
        }
    }

    @Test
    public void testFallbackToLocalWhenRedisFails() {
        LocalStateStore localStore = newLocalStore();
        DistributedStateStore failingRedis = new DistributedStateStore() {
            @Override
            public String getType() {
                return "redis";
            }

            @Override
            public boolean setIfAbsent(String key, String value, long ttlSeconds) {
                throw new RedisConnectionFailureException("redis down");
            }

            @Override
            public String get(String key, long ttlSeconds) {
                throw new RedisConnectionFailureException("redis down");
            }

            @Override
            public void set(String key, String value, long ttlSeconds) {
                throw new RedisConnectionFailureException("redis down");
            }

            @Override
            public boolean delete(String key, long ttlSeconds) {
                throw new RedisConnectionFailureException("redis down");
            }

            @Override
            public boolean exists(String key, long ttlSeconds) {
                throw new RedisConnectionFailureException("redis down");
            }

            @Override
            public boolean expire(String key, long ttlSeconds) {
                throw new RedisConnectionFailureException("redis down");
            }

            @Override
            public long increment(String key, long delta, long ttlSeconds) {
                throw new RedisConnectionFailureException("redis down");
            }

            @Override
            public long deleteByPrefix(String keyPrefix) {
                throw new RedisConnectionFailureException("redis down");
            }
        };
        FallbackStateStore store = new FallbackStateStore(failingRedis, localStore, 60L);
        Assert.assertEquals("redis", store.getType());
        Assert.assertTrue("Redis故障时应降级到本地存储", store.setIfAbsent("fallback-key", "v", 60L));
        Assert.assertEquals("v", store.get("fallback-key", 60L));
    }
}
