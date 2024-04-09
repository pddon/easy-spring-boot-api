package com.pddon.framework.easyapi.dao.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;

@Slf4j
public class RedisUtil {
    public static Boolean isConnected(RedisTemplate redisTemplate) {
        try{
            RedisConnectionFactory redisConnectionFactory = redisTemplate.getConnectionFactory();
            RedisConnection redisConnection = redisConnectionFactory.getConnection();
            Boolean flag = redisConnection.isClosed();
            if (flag) {
                log.info("time: [{}] Redis Connection is Closed.", new Date());
            }
            return !flag;
        }catch (Exception e){
            log.warn("Redis Server connect failed, cause: {}", e.getMessage());
        }
        return false;
    }
}
