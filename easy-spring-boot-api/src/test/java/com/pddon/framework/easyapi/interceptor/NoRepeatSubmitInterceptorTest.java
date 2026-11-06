package com.pddon.framework.easyapi.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.pddon.framework.easyapi.cache.LocalReadCacheContainer;
import com.pddon.framework.easyapi.cache.LocalWriteCacheContainer;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiRestrictions;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.impl.DefaultCacheManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultCheckRepeatManagerImpl;
import com.pddon.framework.easyapi.interceptor.impl.NoRepeatSubmitInterceptor;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;

/**
 * 防重复提交“失败是否释放提交码”的回归：
 * 1、默认（release-on-failure=true）业务失败后释放码，可立即重试；
 * 2、关闭开关时回退旧行为，失败也标记为已提交；
 * 3、未成功获取码的请求不得释放他人持有的码；
 * 4、ThreadLocal在afterInvoke中清理，线程复用时不会串状态。
 */
public class NoRepeatSubmitInterceptorTest {

    private DefaultCheckRepeatManagerImpl checkRepeatManager;
    private NoRepeatSubmitInterceptor interceptor;
    private EasyApiConfig easyApiConfig;

    @Before
    public void setUp() {
        LocalWriteCacheContainer write = new LocalWriteCacheContainer();
        LocalReadCacheContainer read = new LocalReadCacheContainer();
        DefaultCacheManagerImpl cacheManager = new DefaultCacheManagerImpl();
        cacheManager.setLocalWriteCacheContainer(write);
        cacheManager.setLocalReadCacheContainer(read);

        easyApiConfig = new EasyApiConfig();
        checkRepeatManager = new DefaultCheckRepeatManagerImpl();
        checkRepeatManager.setEasyApiConfig(easyApiConfig);
        checkRepeatManager.setCacheManager(cacheManager);

        interceptor = new NoRepeatSubmitInterceptor();
        ReflectionTestUtils.setField(interceptor, "checkRepeatManager", checkRepeatManager);
        ReflectionTestUtils.setField(interceptor, "easyApiConfig", easyApiConfig);

        RequestContext context = RequestContext.getContext();
        context.clear();
        ApiRestrictions restrictions = new ApiRestrictions();
        restrictions.setUniqueSubmit(true);
        context.setApiRestrictions(restrictions);
        context.setAttachment(
                SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.REPEAT_CODE),
                "repeat-code-1");
    }

    @After
    public void tearDown() {
        RequestContext.getContext().clear();
    }

    @Test
    public void testSuccessMarksUsed() throws Throwable {
        interceptor.preInvoke(params(), null, null);
        interceptor.afterInvoke(params(), "success-response", null, null);
        assertUsed("repeat-code-1");
    }

    @Test
    public void testFailureReleasesTokenByDefault() throws Throwable {
        interceptor.preInvoke(params(), null, null);
        interceptor.afterInvoke(params(), failureMarker(), null, null);
        // 失败后码已释放，可立即重试成功
        checkRepeatManager.check("repeat-code-1");
        checkRepeatManager.used("repeat-code-1");
        assertUsed("repeat-code-1");
    }

    @Test
    public void testFailureKeepsOldBehaviorWhenDisabled() throws Throwable {
        easyApiConfig.getNoRepeatSubmit().setReleaseOnFailure(false);
        interceptor.preInvoke(params(), null, null);
        interceptor.afterInvoke(params(), failureMarker(), null, null);
        assertUsed("repeat-code-1");
    }

    @Test
    public void testFailureWithoutAcquiringTokenDoesNotReleaseOthers() throws Throwable {
        // 模拟另一个请求已经持有该码
        checkRepeatManager.check("repeat-code-1");
        // 本请求未成功获取码（preInvoke未执行），失败时不得释放他人持有的码
        interceptor.afterInvoke(params(), failureMarker(), null, null);
        assertUsing("repeat-code-1");
    }

    @Test
    public void testThreadLocalStateDoesNotLeakAcrossRequests() throws Throwable {
        // 第一次请求：获取码后失败，释放
        interceptor.preInvoke(params(), null, null);
        interceptor.afterInvoke(params(), failureMarker(), null, null);
        checkRepeatManager.check("repeat-code-1");

        // 模拟线程复用后的第二次请求，未执行preInvoke：失败时不得释放第一次请求持有的码
        interceptor.afterInvoke(params(), failureMarker(), null, null);
        assertUsing("repeat-code-1");
    }

    private Object failureMarker() {
        Object marker = ReflectionTestUtils.getField(ApiInvokeMethodInterceptorManager.class,
                "INVOKE_FAILED_MARKER");
        Assert.assertNotNull(marker);
        return marker;
    }

    private List<com.pddon.framework.easyapi.dto.ApiRequestParameter> params() {
        return new ArrayList<>();
    }

    private void assertUsed(String key) {
        try {
            checkRepeatManager.check(key);
            Assert.fail("期望防重复提交码已标记为已提交");
        } catch (BusinessException e) {
            Assert.assertEquals("已提交的码应返回UNIQUE_TOKEN_USED",
                    (long) com.pddon.framework.easyapi.consts.ErrorCodes.UNIQUE_TOKEN_USED.getCode(),
                    (long) e.getCode());
        }
    }

    private void assertUsing(String key) {
        try {
            checkRepeatManager.check(key);
            Assert.fail("期望防重复提交码仍处于处理中");
        } catch (BusinessException e) {
            Assert.assertEquals("处理中的码应返回UNIQUE_TOKEN_USING",
                    (long) com.pddon.framework.easyapi.consts.ErrorCodes.UNIQUE_TOKEN_USING.getCode(),
                    (long) e.getCode());
        }
    }
}
