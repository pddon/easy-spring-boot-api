package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.LockDistributedManager;
import com.pddon.framework.easyapi.dao.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LockDistributedManagerImpl implements LockDistributedManager {
    private static final Long RELEASE_SUCCESS = 1L;

    @Autowired
    @Lazy
    private StringRedisTemplate redisStringTemplate;

    private static Boolean connected = null;


    private Boolean isConnected() {
        if(connected != null){
            return connected;
        }
        connected = RedisUtil.isConnected(this.redisStringTemplate);
        return connected;
    }

    @Override
    public String lock(String lockName, long acquireTimeoutSeconds, long timeoutSeconds) {
        if(!this.isConnected()){
            return null;
        }
        // 锁名，即key值
        String lockKey = lockName;
        String requestId = UUID.randomUUID().toString();

        // 获取锁的超时时间，超过这个时间则放弃获取锁
        long end = System.currentTimeMillis() / 1000 + acquireTimeoutSeconds;
        do {//自旋锁，等待锁释放
            if (Boolean.TRUE.equals(redisStringTemplate.opsForValue()
                    .setIfAbsent(lockKey, requestId, timeoutSeconds, TimeUnit.SECONDS))) {
                log.info("获取锁key[{}],value[{}]成功!",lockKey,requestId);
                //获取锁成功
                return requestId;
            } else if (acquireTimeoutSeconds <= 0){
                break;
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } while (System.currentTimeMillis() / 1000 <= end || (acquireTimeoutSeconds <= 0));
        log.info("获取锁key[{}]失败!", lockKey);
        return null;
    }

    @Override
    public boolean unlock(String lockName, String requestId) {
        if(!this.isConnected()){
            return false;
        }
        String lockKey = lockName;
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        boolean re = RELEASE_SUCCESS.equals(redisStringTemplate.execute(
                (RedisConnection connection) -> connection.eval(
                        script.getBytes(),
                        ReturnType.INTEGER,
                        1,
                        lockKey.getBytes(),
                        requestId.getBytes())
        ));
        if(re){
            log.info("解锁key[{}],value[{}]成功!",lockKey,requestId);
        }else{
            log.info("解锁key[{}],value[{}]失败!",lockKey,requestId);
        }
        return re;
    }

    @Override
    public boolean refreshLock(String lockName, String requestId, long timeoutSeconds) {
        if(!this.isConnected()){
            return false;
        }
        String lockKey = lockName;
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('expire', KEYS[1], KEYS[2]) else return 0 end";
        boolean re = RELEASE_SUCCESS.equals(redisStringTemplate.execute(
                (RedisConnection connection) -> connection.eval(
                        script.getBytes(),
                        ReturnType.INTEGER,
                        1,
                        lockKey.getBytes(),
                        requestId.getBytes())
        ));
        if(re){
            log.info("锁续期key[{}],value[{}]成功!", lockKey, requestId);
        }else{
            log.info("锁续期key[{}],value[{}]失败!", lockKey, requestId);
        }
        return re;
    }

    @Override
    public boolean support() {
        return this.isConnected();
    }
}
