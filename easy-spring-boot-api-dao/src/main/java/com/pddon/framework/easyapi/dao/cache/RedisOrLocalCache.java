package com.pddon.framework.easyapi.dao.cache;

import com.pddon.framework.easyapi.dao.utils.PddonRedisCacheReadWriteLock;
import com.pddon.framework.easyapi.dao.utils.RedisCallback;
import com.pddon.framework.easyapi.dao.utils.SerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

@Slf4j
public class RedisOrLocalCache implements Cache {
    private final ReadWriteLock readWriteLock = new PddonRedisCacheReadWriteLock();

    private PerpetualCache defaultCache;

    private static boolean enableRedis = false;

    private String id;

    private static JedisPool pool;

    public RedisOrLocalCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
        this.defaultCache = new PerpetualCache(id);

    }

    public static void setJedisPool(JedisPool pool){
        RedisOrLocalCache.pool = pool;
        RedisOrLocalCache.enableRedis = RedisOrLocalCache.isConnected();
    }

    private static Boolean isConnected() {
        Jedis jedis = null;
        try{
            jedis = RedisOrLocalCache.pool.getResource();
            Boolean flag = jedis.isConnected();
            if (!flag) {
                log.info("time: [{}] Redis Connection is Closed.", new Date());
            }
            return flag;
        }catch (Exception e){
            log.warn("Redis Server connect failed, cause: {}", e.getMessage());
            if(jedis != null){
                jedis.close();
            }
        }
        return false;
    }

    private Object execute(RedisCallback callback) {
        Jedis jedis = pool.getResource();
        try {
            return callback.doWithRedis(jedis);
        } finally {
            if(jedis != null){
                jedis.close();
            }
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getSize() {
        if(this.enableRedis){
            return (Integer) execute(new RedisCallback() {
                @Override
                public Object doWithRedis(Jedis jedis) {
                    Map<byte[], byte[]> result = jedis.hgetAll(id.toString().getBytes());
                    return result.size();
                }
            });
        }else{
            return this.defaultCache.getSize();
        }
    }

    @Override
    public void putObject(final Object key, final Object value) {
        if(this.enableRedis){
            execute(new RedisCallback() {
                @Override
                public Object doWithRedis(Jedis jedis) {
                    jedis.hset(id.toString().getBytes(), key.toString().getBytes(), SerializeUtil.serialize(value));
                    return null;
                }
            });
        }else{
            this.defaultCache.putObject(key, value);
        }
    }

    @Override
    public Object getObject(final Object key) {
        if(this.enableRedis){
            return execute(new RedisCallback() {
                @Override
                public Object doWithRedis(Jedis jedis) {
                    return SerializeUtil.unserialize(jedis.hget(id.toString().getBytes(), key.toString().getBytes()));
                }
            });
        }else{
            return this.defaultCache.getObject(key);
        }
    }

    @Override
    public Object removeObject(final Object key) {
        if(this.enableRedis){
            return execute(new RedisCallback() {
                @Override
                public Object doWithRedis(Jedis jedis) {
                    return jedis.hdel(id.toString(), key.toString());
                }
            });
        }else{
            return this.defaultCache.removeObject(key);
        }
    }

    @Override
    public void clear() {
        if(this.enableRedis){
            execute(new RedisCallback() {
                @Override
                public Object doWithRedis(Jedis jedis) {
                    jedis.del(id.toString());
                    return null;
                }
            });
        }else{
            this.defaultCache.clear();
        }
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        if(this.enableRedis){
            return readWriteLock;
        }else{
            return this.defaultCache.getReadWriteLock();
        }
    }

    @Override
    public String toString() {
        return "RedisOrLocal {" + id + "}";
    }

}
