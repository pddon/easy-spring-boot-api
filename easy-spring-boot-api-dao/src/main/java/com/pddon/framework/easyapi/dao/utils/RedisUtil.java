package com.pddon.framework.easyapi.dao.utils;

import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;

@Slf4j
public class RedisUtil {
    public static Boolean isConnected(RedisTemplate redisTemplate) {
        RedisConnectionFactory redisConnectionFactory = null;
        RedisConnection redisConnection = null;
        try{
            redisConnectionFactory = redisTemplate.getConnectionFactory();
            redisConnection = redisConnectionFactory.getConnection();
            Boolean flag = redisConnection.isClosed();
            if (flag) {
                log.info("time: [{}] Redis Connection is Closed.", new Date());
            }
            return !flag;
        }catch (Exception e){
            log.warn("Redis Server connect failed, cause: {}", e.getMessage());
        }finally {
            if(redisConnection != null && (redisConnectionFactory != null)){
                try{
                    RedisConnectionUtils.releaseConnection(redisConnection, redisConnectionFactory);
                }catch (Exception e){
                    log.warn(IOUtils.getThrowableInfo(e));
                }
            }
        }
        return false;
    }
}
