package com.pddon.framework.easyapi.config;

import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.properties.AsyncThreadPoolConfig;
import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @ClassName: EasyApiAsyncConfigurer
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-12 12:31
 * @Addr: https://pddon.cn
 */
@Configuration
@AutoConfigureAfter(SpringBootApplication.class)
@ConditionalOnMissingBean(AsyncConfigurer.class)
@Slf4j
public class EasyApiAsyncConfigurer implements AsyncConfigurer {

    @Autowired
    private AsyncThreadPoolConfig asyncThreadPoolConfig;

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {

        return new ExceptionHandler();
    };

    @Slf4j(topic="异步线程池运行时异常捕获器")
    static class ExceptionHandler implements AsyncUncaughtExceptionHandler {

        /**
         * @author Allen
         */
        @Override
        public void handleUncaughtException(Throwable e, Method method,
                                            Object... params) {//全局捕获异步执行异常并处理
            log.error(IOUtils.getThrowableInfo(e));
        }

    }

    static class ContextTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            //在任务提交时构建一次快照：此时父线程的请求上下文一定有效，
            //避免任务真正执行时父请求已结束/上下文已清空
            ContextSnapshot snapshot = ContextSnapshot.capture();
            return () -> {
                RequestContext subContext = RequestContext.getContext();
                try {
                    // 将主线程的请求信息，设置到子线程中
                    BeanUtils.copyProperties(snapshot.context, subContext);
                    //共享同一份不可变attachments快照，重复执行该任务时不再重复复制Map
                    subContext.setAttachments(snapshot.attachments);
                    //设置日志链路追踪ID
                    MDC.put("requestId", subContext.getRequestId());
                    // 执行子线程，这一步不要忘了
                    runnable.run();
                } finally {
                    // 线程结束，清空这些信息，否则可能造成内存泄漏
                    subContext.clear();
                }
            };
        }
    }

    /**
     * 异步上下文快照：提交任务时复制一次请求上下文，
     * attachments以不可变Map的形式在任务多次执行之间共享。
     */
    static final class ContextSnapshot {
        private final RequestContext context;
        private final Map<String, Object> attachments;

        private ContextSnapshot(RequestContext context, Map<String, Object> attachments) {
            this.context = context;
            this.attachments = attachments;
        }

        static ContextSnapshot capture() {
            RequestContext source = RequestContext.getContext();
            //触发requestId懒生成，确保快照attachments中已包含requestId；
            //否则子线程首次读取会在不可变快照上写入而抛UnsupportedOperationException
            source.getRequestId();
            RequestContext copy = new RequestContext();
            BeanUtils.copyProperties(source, copy);
            Map<String, Object> sharedAttachments = Collections.unmodifiableMap(
                    new HashMap<>(source.getAttachments()));
            return new ContextSnapshot(copy, sharedAttachments);
        }
    }

    /**
     * 队列满时的拒绝策略常量
     */
    public static final String REJECT_POLICY_DISCARD = "discard";
    public static final String REJECT_POLICY_CALLER_RUNS = "callerRuns";

    /**
     * 未知拒绝策略只提示一次，避免重复创建执行器时反复打日志
     */
    private static final AtomicBoolean UNKNOWN_REJECT_POLICY_WARNED = new AtomicBoolean(false);

    /**
     * @author Allen
     */
    @Override
    public Executor getAsyncExecutor() {
        AsyncThreadPoolConfig config = resolveConfig();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setThreadNamePrefix(config.getThreadNamePrefix());
        executor.setTaskDecorator(new ContextTaskDecorator());
        executor.setRejectedExecutionHandler(buildRejectedExecutionHandler(config.getRejectPolicy()));
        executor.initialize();
        return executor;
    }

    /**
     * 启动期校验线程池配置：
     * fail-fast=true（默认）时保持原有快速失败语义，非法配置直接抛出IllegalArgumentException；
     * fail-fast=false时记录明确日志并回退默认值，避免非法配置把线程池带进不可用状态。
     *
     * 拒绝策略不在校验范围：未知策略沿用原有行为，记录一次告警并使用discard策略。
     */
    AsyncThreadPoolConfig resolveConfig() {
        AsyncThreadPoolConfig config = asyncThreadPoolConfig;
        String error = validate(config);
        if (error == null) {
            return config;
        }
        if (config != null && config.isFailFast()) {
            throw new IllegalArgumentException(error);
        }
        AsyncThreadPoolConfig fallback = new AsyncThreadPoolConfig();
        if (config != null) {
            if (config.getThreadNamePrefix() != null) {
                fallback.setThreadNamePrefix(config.getThreadNamePrefix());
            }
            if (config.getRejectPolicy() != null) {
                fallback.setRejectPolicy(config.getRejectPolicy());
            }
        }
        log.error("异步线程池配置非法[{}]，fail-fast=false，已回退默认值：corePoolSize={}，maxPoolSize={}，queueCapacity={}，keepAliveSeconds={}",
                error, fallback.getCorePoolSize(), fallback.getMaxPoolSize(),
                fallback.getQueueCapacity(), fallback.getKeepAliveSeconds());
        return fallback;
    }

    /**
     * 校验线程池配置，合法返回null，非法返回具体的错误说明
     */
    private String validate(AsyncThreadPoolConfig config) {
        if (config == null) {
            return "easyapi.async-thread-pool configuration is missing";
        }
        Integer corePoolSize = config.getCorePoolSize();
        Integer maxPoolSize = config.getMaxPoolSize();
        Integer queueCapacity = config.getQueueCapacity();
        Integer keepAliveSeconds = config.getKeepAliveSeconds();
        if (corePoolSize == null || corePoolSize < 0) {
            return "corePoolSize must be >= 0, current=" + corePoolSize;
        }
        if (maxPoolSize == null || maxPoolSize < 1) {
            return "maxPoolSize must be >= 1, current=" + maxPoolSize;
        }
        if (maxPoolSize < corePoolSize) {
            return "maxPoolSize must be >= corePoolSize, current maxPoolSize=" + maxPoolSize + ", corePoolSize=" + corePoolSize;
        }
        if (queueCapacity == null || queueCapacity < 0) {
            return "queueCapacity must be >= 0, current=" + queueCapacity;
        }
        if (keepAliveSeconds == null || keepAliveSeconds < 0) {
            return "keepAliveSeconds must be >= 0, current=" + keepAliveSeconds;
        }
        return null;
    }

    /**
     * 根据配置构建拒绝策略：
     * discard（默认）保持原有行为，只记录日志并丢弃任务；
     * callerRuns 由提交任务的线程执行任务，形成背压，不丢任务。
     */
    private RejectedExecutionHandler buildRejectedExecutionHandler(String rejectPolicy) {
        //配置值可能包含空白，忽略首尾空白后再匹配；匹配不区分大小写
        String policy = rejectPolicy == null ? null : rejectPolicy.trim();
        if(REJECT_POLICY_CALLER_RUNS.equalsIgnoreCase(policy)){
            return new ThreadPoolExecutor.CallerRunsPolicy();
        }
        if(policy != null && !policy.isEmpty() && !REJECT_POLICY_DISCARD.equalsIgnoreCase(policy)){
            if(UNKNOWN_REJECT_POLICY_WARNED.compareAndSet(false, true)){
                log.warn("未知的异步线程池拒绝策略[{}]，使用默认的discard策略！", rejectPolicy);
            }
        }
        return new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                log.error("Run out of Async Thread and wait in queue !");
            }
        };
    }

}
