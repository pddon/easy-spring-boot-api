/**
* Title RedisStateStore.java
* Description Redis状态存储实现：多实例共享，关键操作为原子操作
* @author pddon
* @date 2026-11-05
* @version 1.0.0
* site: pddon.cn
*/
package com.pddon.framework.easyapi.state;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * Redis状态存储。
 * <p>所有值为字符串，避免JDK序列化的类版本耦合；key为"全局前缀 + 业务key"，可读可排查。</p>
 * <p>原子性：setIfAbsent对应Redis SET NX EX；increment使用Lua脚本保证INCRBY与EXPIRE的原子性；
 * deleteByPrefix使用SCAN（不使用KEYS，避免阻塞线上实例）。</p>
 * <p>Redis故障时的降级策略由外部包装（FallbackStateStore）决定，本类不做吞异常处理。</p>
 */
public class RedisStateStore implements DistributedStateStore {

	private static final long DEFAULT_TTL_SECONDS = 180L;

	private static final int DELETE_BATCH_SIZE = 500;

	/**
	 * 原子自增并在首次创建时设置过期时间
	 */
	private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>(
			"local v = redis.call('INCRBY', KEYS[1], ARGV[1]); "
			+ "if redis.call('TTL', KEYS[1]) < 0 then redis.call('EXPIRE', KEYS[1], ARGV[2]); end; "
			+ "return v;",
			Long.class);

	private final StringRedisTemplate redisTemplate;

	private final String keyPrefix;

	public RedisStateStore(StringRedisTemplate redisTemplate, String keyPrefix){
		this.redisTemplate = redisTemplate;
		this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
	}

	private long ttl(long ttlSeconds){
		return ttlSeconds > 0 ? ttlSeconds : DEFAULT_TTL_SECONDS;
	}

	private String fullKey(String key){
		return keyPrefix + key;
	}

	@Override
	public String getType() {
		return "redis";
	}

	@Override
	public boolean setIfAbsent(String key, String value, long ttlSeconds) {
		Boolean result = redisTemplate.opsForValue().setIfAbsent(fullKey(key), value, ttl(ttlSeconds), TimeUnit.SECONDS);
		return Boolean.TRUE.equals(result);
	}

	@Override
	public String get(String key, long ttlSeconds) {
		return redisTemplate.opsForValue().get(fullKey(key));
	}

	@Override
	public void set(String key, String value, long ttlSeconds) {
		redisTemplate.opsForValue().set(fullKey(key), value, ttl(ttlSeconds), TimeUnit.SECONDS);
	}

	@Override
	public boolean delete(String key, long ttlSeconds) {
		Boolean result = redisTemplate.delete(fullKey(key));
		return Boolean.TRUE.equals(result);
	}

	@Override
	public boolean exists(String key, long ttlSeconds) {
		Boolean result = redisTemplate.hasKey(fullKey(key));
		return Boolean.TRUE.equals(result);
	}

	@Override
	public boolean expire(String key, long ttlSeconds) {
		Boolean result = redisTemplate.expire(fullKey(key), ttl(ttlSeconds), TimeUnit.SECONDS);
		return Boolean.TRUE.equals(result);
	}

	@Override
	public long increment(String key, long delta, long ttlSeconds) {
		Long result = redisTemplate.execute(INCREMENT_SCRIPT, java.util.Collections.singletonList(fullKey(key)),
				String.valueOf(delta), String.valueOf(ttl(ttlSeconds)));
		return result == null ? 0L : result;
	}

	@Override
	public long deleteByPrefix(String keyPrefix) {
		final String match = fullKey(keyPrefix) + "*";
		Long result = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Long>) connection -> {
			long deleted = 0L;
			List<String> batch = new ArrayList<>(DELETE_BATCH_SIZE);
			Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(match).count(1000).build());
			try{
				while(cursor.hasNext()){
					byte[] key = cursor.next();
					Object deserialized = redisTemplate.getKeySerializer() == null ? key : redisTemplate.getKeySerializer().deserialize(key);
					batch.add(String.valueOf(deserialized));
					if(batch.size() >= DELETE_BATCH_SIZE){
						deleted += deleteBatch(batch);
						batch.clear();
					}
				}
			}finally{
				try{
					cursor.close();
				}catch(Exception ignored){
					// 游标关闭失败不影响删除结果
				}
			}
			if(!batch.isEmpty()){
				deleted += deleteBatch(batch);
			}
			return deleted;
		});
		return result == null ? 0L : result;
	}

	private long deleteBatch(List<String> keys){
		Long result = redisTemplate.delete(keys);
		return result == null ? 0L : result;
	}
}
