package com.pddon.framework.easyapi.impl;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.cache.LocalReadCacheContainer;
import com.pddon.framework.easyapi.cache.LocalWriteCacheContainer;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.dao.utils.RedisUtil;
import com.pddon.framework.easyapi.dto.CacheManagerState;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisCacheManager implements CacheManager, InitializingBean {
    //因为springboot整合redis时会把StringRedisTemplate创建并交于spring容器管理
    @Autowired
    @Lazy
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    @Lazy
    private LocalReadCacheContainer localReadCacheContainer;

    @Autowired
    @Lazy
    private LocalWriteCacheContainer localWriteCacheContainer;

    @Autowired
    private ObjectMapper objectMapper;

    private DefaultCacheManagerImpl defaultCacheManager = new DefaultCacheManagerImpl();

    private static Boolean connected = null;


    private Boolean isConnected() {
        if(connected != null){
            return connected;
        }
        connected = RedisUtil.isConnected(this.redisTemplate);
        return connected;
    }

    @Override
    public void set(String key, Object value, Long expireSeconds) {
        this.set(key, value, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
    }

    @Override
    public void set(String key, Object value, Long expireSeconds, CacheExpireMode mode) {
        if(this.isConnected()){
            String jsonStr = value.toString();
            if(!BeanPropertyUtil.isBaseType(value)){
                try{
                    jsonStr = objectMapper.writeValueAsString(value);
                }catch (Exception e){
                    log.warn(IOUtils.getThrowableInfo(e));
                }
            }
            ValueOperations<String, String> forValue = redisTemplate.opsForValue();
            if(expireSeconds != null && expireSeconds > 0){
                forValue.set(key, jsonStr, expireSeconds, TimeUnit.SECONDS);
            }else{
                forValue.set(key, jsonStr);
            }
        }else{
            this.defaultCacheManager.set(key, value, expireSeconds, mode);
        }
    }

    @Override
    public void set(String key, Object value, Long expireSeconds, Long oldExpireSeconds, CacheExpireMode mode) {
        if(this.isConnected()){
            String jsonStr = value.toString();
            if(!BeanPropertyUtil.isBaseType(value)){
                try{
                    jsonStr = objectMapper.writeValueAsString(value);
                }catch (Exception e){
                    log.warn(IOUtils.getThrowableInfo(e));
                }
            }
            ValueOperations<String, String> forValue = redisTemplate.opsForValue();
            if(expireSeconds != null && expireSeconds > 0){
                forValue.set(key, jsonStr, expireSeconds, TimeUnit.SECONDS);
            }else{
                forValue.set(key, jsonStr);
            }
        }else{
            this.defaultCacheManager.set(key, value, expireSeconds, oldExpireSeconds, mode);
        }
    }

    @Override
    public boolean exists(String key) {
        if(this.isConnected()){
            return redisTemplate.hasKey(key);
        }else {
            return this.defaultCacheManager.exists(key);
        }
    }

    @Override
    public boolean exists(String key, Long expireSeconds) {
        if(this.isConnected()){
            return redisTemplate.hasKey(key);
        }else {
            return this.defaultCacheManager.exists(key, expireSeconds);
        }
    }

    @Override
    public boolean exists(String key, Long expireSeconds, CacheExpireMode mode) {
        if(this.isConnected()){
            return redisTemplate.hasKey(key);
        }else {
            return this.defaultCacheManager.exists(key, expireSeconds, mode);
        }
    }

    @Override
    public boolean exists(String key, CacheExpireMode mode) {
        if(this.isConnected()){
            return redisTemplate.hasKey(key);
        }else {
            return this.defaultCacheManager.exists(key, mode);
        }
    }

    @Override
    @Deprecated
    public Object get(String key, Long expireSeconds, CacheExpireMode mode) {
        if(this.isConnected()){
            try{
                ValueOperations<String, String> forValue = redisTemplate.opsForValue();
                String value = forValue.get(key);
                if(value == null){
                    return null;
                }
                try{
                    return JSONUtil.parseObj(value);
                }catch (Exception e){
                    return value;
                }
            }catch (Exception e){
                log.warn(IOUtils.getThrowableInfo(e));
                return null;
            }finally {
                if(CacheExpireMode.EXPIRE_AFTER_REDA.equals(mode)){
                    this.setExpire(key, expireSeconds);
                }
            }
        }else {
            return this.defaultCacheManager.get(key, expireSeconds, mode);
        }
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        if(this.isConnected()){
            ValueOperations<String, String> forValue = redisTemplate.opsForValue();
            try{
                String value = forValue.get(key);
                if(value == null){
                    return null;
                }
                try{
                    return objectMapper.readValue(value, type);
                }catch (Exception e){
                    return (T) value;
                }
            }catch (Exception e){
                log.warn(IOUtils.getThrowableInfo(e));
                return null;
            }
        }else {
            return this.defaultCacheManager.get(key, type);
        }
    }

    @Override
    public <T> T get(String key, Class<T> type, Long expireSeconds) {
        if(this.isConnected()){
            ValueOperations<String, String> forValue = redisTemplate.opsForValue();
            try{
                String value = forValue.get(key);
                if(value == null){
                    return null;
                }
                try{
                    return objectMapper.readValue(value, type);
                }catch (Exception e){
                    return (T) value;
                }
            }catch (Exception e){
                log.warn(IOUtils.getThrowableInfo(e));
                return null;
            }
        }else {
            return this.defaultCacheManager.get(key, type, expireSeconds);
        }
    }

    @Override
    public <T> T get(String key, Class<T> type, CacheExpireMode mode) {
        if(this.isConnected()){
            ValueOperations<String, String> forValue = redisTemplate.opsForValue();
            try{
                String value = forValue.get(key);
                if(value == null){
                    return null;
                }
                try{
                    return objectMapper.readValue(value, type);
                }catch (Exception e){
                    return (T) value;
                }
            }catch (Exception e){
                log.warn(IOUtils.getThrowableInfo(e));
                return null;
            }
        }else {
            return this.defaultCacheManager.get(key, type, mode);
        }
    }

    @Override
    public <T> T get(String key, Class<T> type, Long expireSeconds, CacheExpireMode mode) {
        if(this.isConnected()){
            ValueOperations<String, String> forValue = redisTemplate.opsForValue();
            try{
                String value = forValue.get(key);
                if(value == null){
                    return null;
                }
                try{
                    return objectMapper.readValue(value, type);
                }catch (Exception e){
                    return (T) value;
                }finally {
                    if(CacheExpireMode.EXPIRE_AFTER_REDA.equals(mode)){
                        this.setExpire(key, expireSeconds);
                    }
                }
            }catch (Exception e){
                log.warn(IOUtils.getThrowableInfo(e));
                return null;
            }
        }else {
            return this.defaultCacheManager.get(key, type, expireSeconds, mode);
        }
    }

    @Override
    public void setExpire(String key, Long expireSeconds) {
        if(this.isConnected()){
            if(expireSeconds != null && expireSeconds > 0){
                this.redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
            }
        }else {
            this.defaultCacheManager.setExpire(key, expireSeconds);
        }
    }

    @Override
    public void setExpire(String key, Long expireSeconds, Long oldExpireSeconds) {
        if(this.isConnected()){
            if(expireSeconds != null && expireSeconds > 0){
                this.redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
            }
        }else {
            this.defaultCacheManager.setExpire(key, expireSeconds, oldExpireSeconds);
        }
    }

    @Override
    public void setExpire(String key, Long expireSeconds, Long oldExpireSeconds, CacheExpireMode mode) {
        if(this.isConnected()){
            if(expireSeconds != null && expireSeconds > 0){
                this.redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
            }
        }else {
            this.defaultCacheManager.setExpire(key, expireSeconds, oldExpireSeconds, mode);
        }
    }

    @Override
    public void setExpire(String key, Long expireSeconds, CacheExpireMode mode) {
        if(this.isConnected()){
            if(expireSeconds != null && expireSeconds > 0){
                this.redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
            }
        }else {
            this.defaultCacheManager.setExpire(key, expireSeconds, mode);
        }
    }

    @Override
    public void remove(String key) {
        if(this.isConnected()){
            this.redisTemplate.delete(key);
        }else {
            this.defaultCacheManager.remove(key);
        }
    }

    @Override
    public void remove(String key, Long expireSeconds) {
        if(this.isConnected()){
            this.redisTemplate.delete(key);
        }else {
            this.defaultCacheManager.remove(key, expireSeconds);
        }
    }

    @Override
    public void remove(String key, Long expireSeconds, CacheExpireMode mode) {
        if(this.isConnected()){
            this.redisTemplate.delete(key);
        }else {
            this.defaultCacheManager.remove(key, expireSeconds, mode);
        }
    }

    @Override
    public void remove(String key, CacheExpireMode mode) {
        if(this.isConnected()){
            this.redisTemplate.delete(key);
        }else {
            this.defaultCacheManager.remove(key, mode);
        }
    }

    @Override
    public void clear() {
        if(this.isConnected()){
            //暂时不支持
            log.warn("Redis cache not support clear all keys.");
        }else {
            this.defaultCacheManager.clear();
        }
    }

    @Override
    public void clear(Long expireSeconds) {
        if(this.isConnected()){
            //暂时不支持
            log.warn("Redis cache not support clear all keys.");
        }else {
            this.defaultCacheManager.clear(expireSeconds);
        }
    }

    @Override
    public Map<String, CacheManagerState> stats(CacheExpireMode mode) {
        if(this.isConnected()){
            //暂时不支持
            log.warn("Redis cache not support clear all keys.");
            return Collections.emptyMap();
        }else {
            return this.defaultCacheManager.stats(mode);
        }
    }

    @Override
    public Map<String, Map<String, Object>> getAllCaches(CacheExpireMode mode) {
        if(this.isConnected()){
            //暂时不支持
            log.warn("Redis cache not support clear all keys.");
            return Collections.emptyMap();
        }else {
            return this.defaultCacheManager.getAllCaches(mode);
        }
    }

    @Override
    public Map<String, Object> getCaches(Long expireSeconds, CacheExpireMode mode) {
        if(this.isConnected()){
            //暂时不支持
            log.warn("Redis cache not support clear all keys.");
            return Collections.emptyMap();
        }else {
            return this.defaultCacheManager.getCaches(expireSeconds, mode);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.defaultCacheManager.setLocalReadCacheContainer(this.localReadCacheContainer);
        this.defaultCacheManager.setLocalWriteCacheContainer(this.localWriteCacheContainer);
    }
}
