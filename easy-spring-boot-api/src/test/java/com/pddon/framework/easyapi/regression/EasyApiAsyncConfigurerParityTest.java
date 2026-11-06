package com.pddon.framework.easyapi.regression;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.pddon.framework.easyapi.config.EasyApiAsyncConfigurer;

import lombok.extern.slf4j.Slf4j;

/**
 * 优化点6的对照测试：异步线程池拒绝策略旧实现（匿名丢弃handler）vs 生产实现。
 */
public class EasyApiAsyncConfigurerParityTest {

    @Test
    public void testDiscardPolicyMatchesLegacyAnonymousHandler() {
        RejectedExecutionHandler produced = build("discard");
        Assert.assertEquals(2, executeWith(produced));
        Assert.assertEquals(2, executeWith(new LegacyDiscardHandler()));
    }

    @Test
    public void testCallerRunsPolicyExecutesRejectedTaskInCallerThread() {
        RejectedExecutionHandler produced = build("callerRuns");
        Assert.assertEquals(3, executeWith(produced));
        Assert.assertEquals(3, executeWith(new ThreadPoolExecutor.CallerRunsPolicy()));
        Assert.assertTrue(produced instanceof ThreadPoolExecutor.CallerRunsPolicy);
    }

    @Test
    public void testUnknownPolicyFallsBackToLegacyDiscardBehavior() {
        for (String policy : new String[]{"bogus", "", "  ", null}) {
            Assert.assertEquals("policy=" + policy, 2, executeWith(build(policy)));
            Assert.assertEquals("legacy policy=" + policy, 2, executeWith(new LegacyDiscardHandler()));
        }
    }

    @Test
    public void testPolicyIgnoresWhitespaceAndCase() {
        Assert.assertEquals(3, executeWith(build(" CALLERRUNS ")));
        Assert.assertEquals(3, executeWith(build("CallerRuns")));
        Assert.assertEquals(2, executeWith(build(" DISCARD ")));
    }

    private static RejectedExecutionHandler build(String policy) {
        return (RejectedExecutionHandler) ReflectionTestUtils.invokeMethod(
                new EasyApiAsyncConfigurer(), "buildRejectedExecutionHandler", policy);
    }

    /**
     * 1个核心线程 + 1个队列元素，提交3个任务：
     * 丢弃策略执行2个；callerRuns 策略第3个由提交线程执行，共3个。
     */
    private static int executeWith(RejectedExecutionHandler handler) {
        final AtomicInteger executed = new AtomicInteger();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1), handler);
        for (int i = 0; i < 3; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    executed.incrementAndGet();
                }
            });
        }
        executor.shutdown();
        try {
            Assert.assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail("等待线程池结束被中断");
        }
        return executed.get();
    }

    /**
     * HEAD 中的拒绝策略：只记录日志并丢弃任务。
     */
    @Slf4j
    static class LegacyDiscardHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.error("Run out of Async Thread and wait in queue !");
        }
    }
}
