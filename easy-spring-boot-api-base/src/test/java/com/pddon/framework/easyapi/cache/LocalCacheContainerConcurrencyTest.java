package com.pddon.framework.easyapi.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.pddon.framework.easyapi.consts.CacheExpireMode;

/**
 * 本地缓存容器并发压力测试：
 * 多线程执行set/get/remove/clear，验证：
 * 1、写入后读到的数据不为null且始终是写入该key的值（无脏读、无丢失）；
 * 2、remove/clear后不再读到旧数据（无残留）；
 * 3、缓存管理器数量不会因并发写入失控（无桶泄漏）。
 * 同时覆盖“保留全局锁（原行为）”与“关闭全局锁（可选优化）”两种模式。
 */
public class LocalCacheContainerConcurrencyTest {

    private static final int THREADS = 8;
    private static final int KEYS_PER_THREAD = 200;
    private static final int ROUNDS = 5;

    @Test
    public void testConcurrentPutGetRemoveWithGlobalLock() throws Exception {
        runStress(false);
    }

    @Test
    public void testConcurrentPutGetRemoveWithoutGlobalLock() throws Exception {
        runStress(true);
    }

    @Test
    public void testSetWithDifferentExpireRemovesOldBucket() {
        LocalCacheContainer container = new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, true);
        container.set("same-key", "v1", 60L);
        Assert.assertEquals("v1", container.get("same-key"));
        container.set("same-key", "v2", 120L);
        Assert.assertEquals("v2", container.get("same-key"));
        // 旧的过期桶中不应残留该key
        Assert.assertNull(container.get("same-key", 60L));
    }

    /**
     * 缓存管理器（过期桶）数量达到上限时，remove必须删除定位到的桶中的key。
     * 旧实现先locate到过期时效，再通过getCache取管理器；getCache在数量超限时会回退到
     * 默认管理器，导致目标key实际未被删除，形成“删除后仍可读到”的残留。
     */
    @Test
    public void testRemoveWorksWhenCacheManagerCountReachesLimit() {
        LocalCacheContainer container = new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, false);
        for (int i = 1; i <= 2000; i++) {
            container.set("k-" + i, "v-" + i, (long) i);
        }
        container.set("target-key", "target-value", 1000L);
        Assert.assertEquals("target-value", container.get("target-key"));

        container.remove("target-key");
        Assert.assertNull("达到缓存管理器上限后，remove仍必须删除定位到的桶中的key",
                container.get("target-key"));
        Assert.assertNull(container.get("target-key", 1000L));
    }

    private void runStress(boolean lockFree) throws Exception {
        LocalCacheContainer container = new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, lockFree);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        AtomicBoolean failed = new AtomicBoolean(false);
        AtomicInteger dirtyReads = new AtomicInteger();
        ConcurrentHashMap<String, String> expectedValues = new ConcurrentHashMap<>();

        for (int t = 0; t < THREADS; t++) {
            final int threadId = t;
            pool.execute(() -> {
                try {
                    start.await();
                    for (int i = 0; i < KEYS_PER_THREAD; i++) {
                        String key = "k-" + threadId + "-" + i;
                        String value = "v-" + threadId + "-" + i;
                        expectedValues.put(key, value);
                        container.set(key, value, 60L);
                        Object read = container.get(key);
                        if (read != null && !value.equals(read)) {
                            dirtyReads.incrementAndGet();
                        }
                    }
                    // 多轮并发set/get/remove，验证remove后无残留
                    for (int round = 0; round < ROUNDS; round++) {
                        for (int i = 0; i < KEYS_PER_THREAD; i++) {
                            String key = "k-" + threadId + "-" + i;
                            String value = "v-" + threadId + "-" + i;
                            container.set(key, value, 60L);
                            Object read = container.get(key);
                            if (!value.equals(read)) {
                                dirtyReads.incrementAndGet();
                            }
                            container.remove(key);
                            if (container.get(key) != null) {
                                dirtyReads.incrementAndGet();
                            }
                        }
                    }
                } catch (Throwable e) {
                    failed.set(true);
                    e.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        boolean finished = done.await(60, TimeUnit.SECONDS);
        pool.shutdownNow();
        Assert.assertTrue("并发测试未在预期时间内完成", finished);
        Assert.assertFalse("并发执行出现异常", failed.get());
        Assert.assertEquals("存在脏读或数据丢失", 0, dirtyReads.get());

        // 所有key最终都在remove后应不可见
        for (String key : expectedValues.keySet()) {
            Assert.assertNull("remove后仍读到残留数据:" + key, container.get(key));
        }

        // clear后所有数据不可见且缓存桶数量受控
        container.clear(null);
        Assert.assertNull(container.get("any-key"));
        Map<String, Map<String, Object>> caches = container.getCaches(null);
        Assert.assertTrue("缓存管理器数量异常增长:" + caches.size(), caches.size() <= 2);

        // 再次写入并读取，证明clear后容器仍可用
        container.set("after-clear", "ok", 60L);
        Assert.assertEquals("ok", container.get("after-clear"));
    }
}
