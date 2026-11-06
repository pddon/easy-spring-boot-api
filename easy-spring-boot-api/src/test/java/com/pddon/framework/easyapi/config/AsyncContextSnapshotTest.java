package com.pddon.framework.easyapi.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.pddon.framework.easyapi.context.RequestContext;

/**
 * 优化点6的验证：异步任务使用提交时的不可变上下文快照。
 * 1）父请求结束后（context.clear）执行任务，任务仍能拿到提交时的requestId与attachments；
 * 2）attachments在任务的多次执行之间共享同一份不可变Map；
 * 3）任务内修改attachments会抛UnsupportedOperationException（明确不可变语义）。
 */
public class AsyncContextSnapshotTest {

    @After
    public void tearDown() {
        RequestContext.getContext().clear();
    }

    @Test
    public void testSnapshotSurvivesParentContextClear() {
        String requestId = fillParentContext("trace-1");
        final List<String> observed = new ArrayList<>();

        Runnable decorated = new EasyApiAsyncConfigurer.ContextTaskDecorator().decorate(new Runnable() {
            @Override
            public void run() {
                RequestContext sub = RequestContext.getContext();
                observed.add(sub.getRequestId() + "|" + sub.getAttachments().get("traceId"));
            }
        });

        //模拟HTTP请求已结束并清理线程上下文
        RequestContext.getContext().clear();

        decorated.run();

        Assert.assertEquals(Arrays.asList(requestId + "|trace-1"), observed);
    }

    @Test
    public void testRepeatedExecutionSharesSameImmutableAttachments() {
        fillParentContext("trace-2");
        final List<Object> attachmentMaps = new ArrayList<>();

        Runnable decorated = new EasyApiAsyncConfigurer.ContextTaskDecorator().decorate(new Runnable() {
            @Override
            public void run() {
                RequestContext sub = RequestContext.getContext();
                attachmentMaps.add(sub.getAttachments());
                Assert.assertEquals("trace-2", sub.getAttachments().get("traceId"));
            }
        });

        decorated.run();
        decorated.run();

        Assert.assertEquals(2, attachmentMaps.size());
        Assert.assertSame("两次执行应共享同一份快照", attachmentMaps.get(0), attachmentMaps.get(1));
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> shared = (Map<String, Object>) attachmentMaps.get(0);
            shared.put("mutated", "1");
            Assert.fail("共享快照应为不可变Map");
        } catch (UnsupportedOperationException expected) {
            //期望：快照不可变
        }
    }

    private static String fillParentContext(String traceId) {
        RequestContext context = RequestContext.getContext();
        context.clear();
        Map<String, Object> attachments = new HashMap<>();
        attachments.put("traceId", traceId);
        context.setAttachments(attachments);
        //请求ID由上下文懒生成并写入attachments
        return context.getRequestId();
    }
}
