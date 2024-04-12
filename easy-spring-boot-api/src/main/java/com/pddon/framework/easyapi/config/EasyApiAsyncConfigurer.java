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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName: EasyApiAsyncConfigurer
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-12 12:31
 * @Addr: https://pddon.cn
 */
@Configuration
@AutoConfigureAfter(SpringBootApplication.class)
@Slf4j
public class EasyApiAsyncConfigurer  implements AsyncConfigurer {

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
            //获取父线程的loginVal
            RequestContext requestContext = RequestContext.getContext();
            return () -> {
                RequestContext subContext = RequestContext.getContext();
                try {
                    // 将主线程的请求信息，设置到子线程中
                    BeanUtils.copyProperties(requestContext, subContext);
                    subContext.setAttachments(new HashMap<>(requestContext.getAttachments()));
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
     * @author Allen
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncThreadPoolConfig.getCorePoolSize());
        executor.setMaxPoolSize(asyncThreadPoolConfig.getMaxPoolSize());
        executor.setQueueCapacity(asyncThreadPoolConfig.getQueueCapacity());
        executor.setKeepAliveSeconds(asyncThreadPoolConfig.getKeepAliveSeconds());
        executor.setThreadNamePrefix(asyncThreadPoolConfig.getThreadNamePrefix());
        executor.setTaskDecorator(new ContextTaskDecorator());
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                log.error("Run out of Async Thread and wait in queue !");
            }
        });
        executor.initialize();
        return executor;
    }

}
