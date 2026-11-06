package com.pddon.framework.easyapi.context;

import org.junit.Assert;
import org.junit.Test;

/**
 * 验证请求上下文清理会重置所有请求级状态：
 * 线程被容器复用时，若某个状态未清理（例如ignoreDataPerm），
 * 会在后续请求上继续生效，造成数据权限相关逻辑被绕过。
 */
public class RequestContextClearTest {

    @Test
    public void testClearResetsIgnoreDataPerm() {
        RequestContext context = RequestContext.getContext();
        context.setIgnoreDataPerm(true);
        context.setIgnoreTenant(true);
        context.setSuperManager(true);
        context.setTenantId("tenant");
        context.setAppId("app");

        context.clear();

        Assert.assertFalse("clear后ignoreDataPerm应恢复默认false", context.isIgnoreDataPerm());
        Assert.assertFalse(context.isIgnoreTenant());
        Assert.assertFalse(context.isSuperManager());
        Assert.assertNull(context.getTenantId());
        Assert.assertNull(context.getAppId());
        Assert.assertTrue(context.getAttachments().isEmpty());
        Assert.assertTrue(context.getResponseSystemParams().isEmpty());
    }

    @Test
    public void testDefaultIgnoreDataPermIsFalse() {
        RequestContext context = RequestContext.getContext();
        Assert.assertFalse(context.isIgnoreDataPerm());
    }
}
