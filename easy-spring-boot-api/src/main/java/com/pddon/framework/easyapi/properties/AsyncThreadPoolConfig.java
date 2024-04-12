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
}
