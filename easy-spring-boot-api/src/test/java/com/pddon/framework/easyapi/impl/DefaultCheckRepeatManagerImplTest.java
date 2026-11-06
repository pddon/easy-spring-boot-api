package com.pddon.framework.easyapi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.pddon.framework.easyapi.cache.LocalReadCacheContainer;
import com.pddon.framework.easyapi.cache.LocalWriteCacheContainer;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.properties.EasyApiConfig;

/**
 * 防重复提交校验的并发语义回归：
 * 1、同一个防重复提交码被并发校验时，只允许一个请求通过；
 * 2、已标记提交后再次校验，抛出“已提交”异常；
 * 3、不同码互不影响。
 */
public class DefaultCheckRepeatManagerImplTest {

    private DefaultCheckRepeatManagerImpl checkRepeatManager;

    @Before
    public void setUp() {
        LocalWriteCacheContainer write = new LocalWriteCacheContainer();
        LocalReadCacheContainer read = new LocalReadCacheContainer();
        DefaultCacheManagerImpl cacheManager = new DefaultCacheManagerImpl();
        cacheManager.setLocalWriteCacheContainer(write);
        cacheManager.setLocalReadCacheContainer(read);

        checkRepeatManager = new DefaultCheckRepeatManagerImpl();
        checkRepeatManager.setEasyApiConfig(new EasyApiConfig());
        checkRepeatManager.setCacheManager(cacheManager);
    }

    @Test
    public void testConcurrentCheckOnlyOnePasses() throws Exception {
        final int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger passed = new AtomicInteger();
        AtomicInteger using = new AtomicInteger();
        List<Throwable> unexpected = new ArrayList<>();
        final String key = "same-repeat-code";

        for (int i = 0; i < threads; i++) {
            pool.execute(() -> {
                try {
                    start.await();
                    checkRepeatManager.check(key);
                    passed.incrementAndGet();
                } catch (BusinessException e) {
                    if (ErrorCodes.UNIQUE_TOKEN_USING.getMsgCode().equals(e.getMessage())) {
                        using.incrementAndGet();
                    } else {
                        synchronized (unexpected) {
                            unexpected.add(e);
                        }
                    }
                } catch (Throwable e) {
                    synchronized (unexpected) {
                        unexpected.add(e);
                    }
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        Assert.assertTrue(done.await(30, TimeUnit.SECONDS));
        pool.shutdownNow();

        Assert.assertTrue("出现非预期异常:" + unexpected, unexpected.isEmpty());
        Assert.assertEquals("同一防重复提交码并发校验只应通过一次", 1, passed.get());
        Assert.assertEquals("其余请求应返回处理中异常", threads - 1, using.get());
    }

    @Test
    public void testUsedThenCheckThrowsUsed() {
        String key = "used-code";
        checkRepeatManager.check(key);
        checkRepeatManager.used(key);
        try {
            checkRepeatManager.check(key);
            Assert.fail("已提交的码再次校验应抛出异常");
        } catch (BusinessException e) {
            Assert.assertEquals(ErrorCodes.UNIQUE_TOKEN_USED.getMsgCode(), e.getMessage());
        }
    }

    @Test
    public void testDifferentKeysDoNotBlockEachOther() {
        checkRepeatManager.check("code-a");
        checkRepeatManager.check("code-b");
        checkRepeatManager.check("code-c");
    }
}
