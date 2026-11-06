package com.pddon.framework.easyapi.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.pddon.framework.easyapi.properties.AsyncThreadPoolConfig;

/**
 * 验证异步线程池队列满时的两种拒绝策略：
 * discard（默认，原行为：记录日志并丢弃任务）
 * callerRuns（由提交线程执行，形成背压，不丢任务）
 */
public class EasyApiAsyncConfigurerRejectPolicyTest {

    @Test
    public void testDefaultRejectPolicyIsDiscard() {
        Assert.assertEquals("discard", new AsyncThreadPoolConfig().getRejectPolicy());
    }

    @Test
    public void testDiscardPolicyDropsTaskWhenQueueFull() throws Exception {
        RejectedExecutionHandler handler = buildHandler("discard");
        assertPolicyBehavior(handler, 2, "discard");
    }

    @Test
    public void testCallerRunsPolicyBackpressuresWhenQueueFull() throws Exception {
        RejectedExecutionHandler handler = buildHandler("callerRuns");
        assertPolicyBehavior(handler, 3, "callerRuns");
    }

    @Test
    public void testRejectPolicyIgnoresCaseAndWhitespace() throws Exception {
        RejectedExecutionHandler handler = buildHandler("  CallerRuns  ");
        assertPolicyBehavior(handler, 3, "CallerRuns->callerRuns");
    }

    @Test
    public void testUnknownPolicyFallsBackToDiscard() throws Exception {
        RejectedExecutionHandler handler = buildHandler("unknown");
        assertPolicyBehavior(handler, 2, "unknown->discard");
    }

    private RejectedExecutionHandler buildHandler(String policy) {
        EasyApiAsyncConfigurer configurer = new EasyApiAsyncConfigurer();
        ReflectionTestUtils.setField(configurer, "asyncThreadPoolConfig", new AsyncThreadPoolConfig());
        return ReflectionTestUtils.invokeMethod(configurer, "buildRejectedExecutionHandler", policy);
    }

    /**
     * 线程池容量：1个核心线程 + 1个队列位；先提交阻塞任务占满线程，再提交一个排队任务，
     * 第三个任务触发拒绝策略。
     */
    private void assertPolicyBehavior(RejectedExecutionHandler handler, int expectedExecuted, String policy)
            throws Exception {
        CountDownLatch blocking = new CountDownLatch(1);
        CountDownLatch firstStarted = new CountDownLatch(1);
        AtomicInteger executed = new AtomicInteger();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1), handler);

        executor.execute(() -> {
            firstStarted.countDown();
            try {
                blocking.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            executed.incrementAndGet();
        });

        Assert.assertTrue(firstStarted.await(2, TimeUnit.SECONDS));
        // 第二个任务进入队列
        executor.execute(executed::incrementAndGet);
        // 第三个任务触发拒绝策略
        executor.execute(executed::incrementAndGet);

        blocking.countDown();
        executor.shutdown();
        Assert.assertTrue("线程池未在预期时间内结束，策略=" + policy,
                executor.awaitTermination(5, TimeUnit.SECONDS));
        Assert.assertEquals("策略=" + policy + " 的执行任务数", expectedExecuted, executed.get());
    }
}
