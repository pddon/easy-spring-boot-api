package com.pddon.framework.easyapi.security;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.pddon.framework.easyapi.cache.LocalWriteCacheContainer;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.state.LocalStateStore;

/**
 * 验签防重放校验器回归：
 * 时间窗、nonce合法性、nonce一次性（含并发），以及默认关闭时的配置默认值。
 */
public class SignReplayProtectorTest {

    private LocalStateStore localStore() {
        return new LocalStateStore(new LocalWriteCacheContainer(), "easyapi:test:");
    }

    private SignReplayProtector protector(boolean enabled, long windowSeconds, long nonceTtlSeconds) {
        EasyApiConfig config = new EasyApiConfig();
        EasyApiConfig.Sign.ReplayProtection replay = config.getSign().getReplayProtection();
        replay.setEnabled(enabled);
        replay.setTimestampWindowSeconds(windowSeconds);
        replay.setNonceTtlSeconds(nonceTtlSeconds);
        return new SignReplayProtector(config, localStore());
    }

    @Test
    public void testDefaultConfigMatchesCodeDefaults() {
        EasyApiConfig config = new EasyApiConfig();
        Assert.assertTrue("默认失败时应释放防重复提交码", config.getNoRepeatSubmit().isReleaseOnFailure());
        EasyApiConfig.Sign.ReplayProtection replay = config.getSign().getReplayProtection();
        Assert.assertFalse("防重放默认关闭", replay.isEnabled());
        Assert.assertEquals(300L, replay.getTimestampWindowSeconds());
        Assert.assertEquals(300L, replay.getNonceTtlSeconds());
        Assert.assertEquals(100000, replay.getNonceCacheSize());
        Assert.assertEquals("nonce", replay.getNonceParam());
        Assert.assertEquals(128, replay.getNonceMaxLength());
        Assert.assertFalse(new SignReplayProtector(config, localStore()).isEnabled());
    }

    @Test
    public void testTimestampInsideWindowPasses() {
        protector(true, 300L, 300L).checkTimestamp(String.valueOf(System.currentTimeMillis()), "timestamp");
    }

    @Test
    public void testTimestampOutsideWindowRejected() {
        SignReplayProtector protector = protector(true, 300L, 300L);
        String expired = String.valueOf(System.currentTimeMillis() - 301L * 1000L);
        try {
            protector.checkTimestamp(expired, "timestamp");
            Assert.fail("超出时间窗应被拒绝");
        } catch (BusinessException e) {
            Assert.assertEquals((long) ErrorCodes.ERROR_SIGN.getCode(), (long) e.getCode());
        }
        String future = String.valueOf(System.currentTimeMillis() + 301L * 1000L);
        try {
            protector.checkTimestamp(future, "timestamp");
            Assert.fail("未来时间超出时间窗应被拒绝");
        } catch (BusinessException e) {
            Assert.assertEquals((long) ErrorCodes.ERROR_SIGN.getCode(), (long) e.getCode());
        }
    }

    @Test
    public void testInvalidTimestampRejected() {
        try {
            protector(true, 300L, 300L).checkTimestamp("not-a-number", "timestamp");
            Assert.fail("非法时间戳应被拒绝");
        } catch (BusinessException e) {
            Assert.assertEquals((long) ErrorCodes.ERROR_SIGN.getCode(), (long) e.getCode());
        }
    }

    @Test
    public void testNonceRequiredAndLengthLimited() {
        SignReplayProtector protector = protector(true, 300L, 300L);
        assertSignError(protector, null);
        assertSignError(protector, "  ");
        StringBuilder tooLong = new StringBuilder();
        for (int i = 0; i <= 128; i++) {
            tooLong.append('a');
        }
        assertSignError(protector, tooLong.toString());
        protector.requireValidNonce("nonce-1");
    }

    @Test
    public void testNonceCanOnlyBeUsedOnce() {
        SignReplayProtector protector = protector(true, 300L, 300L);
        protector.consumeNonce("nonce-1");
        try {
            protector.consumeNonce("nonce-1");
            Assert.fail("重复nonce应被拒绝");
        } catch (BusinessException e) {
            Assert.assertEquals((long) ErrorCodes.REPLAY_ATTACK.getCode(), (long) e.getCode());
        }
        protector.consumeNonce("nonce-2");
    }

    @Test
    public void testConcurrentSameNonceOnlyOnePasses() throws Exception {
        final SignReplayProtector protector = protector(true, 300L, 300L);
        final int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger passed = new AtomicInteger();
        List<Throwable> unexpected = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            pool.execute(() -> {
                try {
                    start.await();
                    protector.consumeNonce("same-nonce");
                    passed.incrementAndGet();
                } catch (BusinessException e) {
                    if (ErrorCodes.REPLAY_ATTACK.getCode() != e.getCode()) {
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
        Assert.assertEquals("同一nonce并发消费只应成功一次", 1, passed.get());
    }

    @Test
    public void testNonceExpiresAfterTtl() throws Exception {
        EasyApiConfig config = new EasyApiConfig();
        EasyApiConfig.Sign.ReplayProtection replay = config.getSign().getReplayProtection();
        replay.setEnabled(true);
        replay.setNonceTtlSeconds(1L);
        SignReplayProtector protector = new SignReplayProtector(config, localStore());
        protector.consumeNonce("ttl-nonce");
        Thread.sleep(1500L);
        // TTL过期后nonce会被释放，重新签名的合法请求不应被永久拒绝
        protector.consumeNonce("ttl-nonce");
    }

    @Test
    public void testSameNonceAcrossInstancesOnlyOneConsumes() {
        // 两个"实例"共享同一个状态存储，模拟接入Redis后的多实例场景
        LocalStateStore sharedStore = localStore();
        EasyApiConfig configA = new EasyApiConfig();
        configA.getSign().getReplayProtection().setEnabled(true);
        EasyApiConfig configB = new EasyApiConfig();
        configB.getSign().getReplayProtection().setEnabled(true);
        SignReplayProtector instanceA = new SignReplayProtector(configA, sharedStore);
        SignReplayProtector instanceB = new SignReplayProtector(configB, sharedStore);
        instanceA.consumeNonce("shared-nonce");
        try {
            instanceB.consumeNonce("shared-nonce");
            Assert.fail("跨实例重复使用nonce应被拒绝");
        } catch (BusinessException e) {
            Assert.assertEquals((long) ErrorCodes.REPLAY_ATTACK.getCode(), (long) e.getCode());
        }
    }

    private void assertSignError(SignReplayProtector protector, String nonce) {
        try {
            protector.requireValidNonce(nonce);
            Assert.fail("非法nonce应被拒绝:" + nonce);
        } catch (BusinessException e) {
            Assert.assertEquals((long) ErrorCodes.ERROR_SIGN.getCode(), (long) e.getCode());
        }
    }
}
