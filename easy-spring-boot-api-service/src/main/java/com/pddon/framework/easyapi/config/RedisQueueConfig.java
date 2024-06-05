package com.pddon.framework.easyapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @ClassName: RedisQueueConfig
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-06 01:29
 * @Addr: https://pddon.cn
 */
@Configuration
@Slf4j
public class RedisQueueConfig {
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisTemplate<String, String> redisTemplate) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisTemplate.getConnectionFactory());
        return container;
    }
}
