/**
* Title LocalStateStore.java
* Description 本地状态存储实现：基于框架原有的本地缓存容器，单JVM内有效
* @author pddon
* @date 2026-11-05
* @version 1.0.0
* site: pddon.cn
*/
package com.pddon.framework.easyapi.state;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.pddon.framework.easyapi.cache.LocalCacheContainer;

/**
 * 本地状态存储。
 * <p>复用 LocalCacheContainer 的分桶缓存：写入按ttlSeconds分桶，读取/删除按key全局定位，
 * 与Redis实现的key级TTL语义保持一致（ttlSeconds只作为写入时效）。</p>
 * <p>本实现的原子性只在单JVM内成立：setIfAbsent 通过分片锁串行化"判断+写入"，
 * 多实例部署时请使用Redis实现。</p>
 */
public class LocalStateStore implements DistributedStateStore {

	private static final int LOCK_STRIPES = 64;

	private static final long DEFAULT_TTL_SECONDS = 180L;

	private final LocalCacheContainer cacheContainer;

	private final String keyPrefix;

	private final Object[] locks = new Object[LOCK_STRIPES];

	{
		for(int i = 0; i < LOCK_STRIPES; i++){
			locks[i] = new Object();
		}
	}

	public LocalStateStore(LocalCacheContainer cacheContainer, String keyPrefix){
		this.cacheContainer = cacheContainer;
		this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
	}

	private Object lockOf(String key){
		int hash = key == null ? 0 : key.hashCode();
		return locks[(hash & 0x7fffffff) % LOCK_STRIPES];
	}

	private long ttl(long ttlSeconds){
		return ttlSeconds > 0 ? ttlSeconds : DEFAULT_TTL_SECONDS;
	}

	private String fullKey(String key){
		return keyPrefix + key;
	}

	@Override
	public String getType() {
		return "local";
	}

	@Override
	public boolean setIfAbsent(String key, String value, long ttlSeconds) {
		long ttl = ttl(ttlSeconds);
		String k = fullKey(key);
		synchronized (lockOf(k)) {
			//ttlSeconds只作为写入时效，不参与key定位：与Redis实现的key级TTL语义保持一致，
			//避免同一key以不同TTL写入/读取时在本地实现中出现"存在却读不到"
			if(cacheContainer.exists(k)){
				return false;
			}
			cacheContainer.set(k, value, ttl);
			return true;
		}
	}

	@Override
	public String get(String key, long ttlSeconds) {
		Object value = cacheContainer.get(fullKey(key));
		return value == null ? null : value.toString();
	}

	@Override
	public void set(String key, String value, long ttlSeconds) {
		cacheContainer.set(fullKey(key), value, ttl(ttlSeconds));
	}

	@Override
	public boolean delete(String key, long ttlSeconds) {
		String k = fullKey(key);
		boolean exists = cacheContainer.exists(k);
		cacheContainer.remove(k);
		return exists;
	}

	@Override
	public boolean exists(String key, long ttlSeconds) {
		return cacheContainer.exists(fullKey(key));
	}

	@Override
	public boolean expire(String key, long ttlSeconds) {
		String k = fullKey(key);
		if(!cacheContainer.exists(k)){
			return false;
		}
		cacheContainer.setExpire(k, ttl(ttlSeconds));
		return true;
	}

	@Override
	public long increment(String key, long delta, long ttlSeconds) {
		long ttl = ttl(ttlSeconds);
		String k = fullKey(key);
		synchronized (lockOf(k)) {
			Object value = cacheContainer.get(k);
			long current = 0L;
			if(value != null){
				try{
					current = Long.parseLong(value.toString());
				}catch(NumberFormatException e){
					current = 0L;
				}
			}
			long next = current + delta;
			cacheContainer.set(k, String.valueOf(next), ttl);
			return next;
		}
	}

	@Override
	public long deleteByPrefix(String keyPrefix) {
		long deleted = 0L;
		Map<String, Map<String, Object>> caches = cacheContainer.getCaches(null);
		if(caches == null){
			return deleted;
		}
		for(Map.Entry<String, Map<String, Object>> bucketEntry : caches.entrySet()){
			// 桶key形如"WRITE:180"，取最后一段作为过期秒数
			String bucketKey = bucketEntry.getKey();
			Long bucketTtl;
			try{
				bucketTtl = Long.valueOf(bucketKey.substring(bucketKey.lastIndexOf(':') + 1));
			}catch(NumberFormatException e){
				continue;
			}
			long ttl = ttl(bucketTtl);
			String prefix = fullKey(keyPrefix);
			for(String cacheKey : new java.util.ArrayList<>(bucketEntry.getValue().keySet())){
				if(cacheKey.startsWith(prefix)){
					cacheContainer.remove(cacheKey, ttl);
					deleted++;
				}
			}
		}
		return deleted;
	}
}
