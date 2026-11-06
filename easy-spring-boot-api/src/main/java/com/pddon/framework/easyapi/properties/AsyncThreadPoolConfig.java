package com.pddon.framework.easyapi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName: AsyncThreadPoolConfig
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-12 12:37
 * @Addr: https://pddon.cn
 */
@Component
@ConfigurationProperties(prefix="easyapi.async-thread-pool")
@Data
public class AsyncThreadPoolConfig {
    /**
     * 核心线程数
     */
    private Integer corePoolSize = 5;
    /**
     * 最大线程数
     */
    private Integer maxPoolSize = 20;
    /**
     * 队列最大长度，等待队列最大值
     */
    private Integer queueCapacity = 10000;
    /**
     * 线程池维护线程所允许的空闲时间
     */
    private Integer keepAliveSeconds = 600;
    /**
     * 线程名前缀
     */
    private String threadNamePrefix = "EasyApi-AsyncExecutor-";
    /**
     * 队列满时的拒绝策略：
     * discard（默认，记录错误日志并丢弃任务，保持原有行为）
     * callerRuns（由提交任务的线程执行，形成背压，不丢任务）
     */
    private String rejectPolicy = "discard";
    /**
     * 启动期配置校验失败时是否快速失败，对应配置项easyapi.async-thread-pool.fail-fast，默认true。
     *
     * true：配置非法时抛出IllegalArgumentException，应用启动失败，避免把错误配置带到运行期；
     * false：记录ERROR日志并回退到默认值（corePoolSize=5、maxPoolSize=20、
     *        queueCapacity=10000、keepAliveSeconds=600），应用继续启动。
     * 建议保持默认true；仅在配置来自不可控来源、允许降级时开启false。
     */
    private boolean failFast = true;
}
