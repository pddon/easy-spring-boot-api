/**
* Title FallbackStateStore.java
* Description Redis状态存储的故障降级包装：Redis不可用时按配置回退本地存储或直接抛出异常
* @author pddon
* @date 2026-11-05
* @version 1.0.0
* site: pddon.cn
*/
package com.pddon.framework.easyapi.state;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.springframework.dao.DataAccessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis故障降级包装。
 * <p>fallbackToLocalOnFailure=true（默认）：Redis异常时回退本地存储并打印限频WARN日志。
 * 业务影响：请求不会被拒绝，但降级期间各实例的状态不再共享，可能出现跨实例重复提交/重放，
 * 需要在监控上关注该WARN日志。</p>
 * <p>fallbackToLocalOnFailure=false（fail-fast）：异常向上抛出，请求失败。
 * 业务影响：可用性下降，但不会出现"看似防重、实际不防重"的情况。</p>
 */
@Slf4j
public class FallbackStateStore implements DistributedStateStore {

	private final DistributedStateStore primary;

	private final DistributedStateStore fallback;

	private final long warnIntervalMillis;

	private final AtomicLong lastWarnTime = new AtomicLong(0L);

	public FallbackStateStore(DistributedStateStore primary, DistributedStateStore fallback, long warnIntervalSeconds){
		this.primary = primary;
		this.fallback = fallback;
		this.warnIntervalMillis = Math.max(warnIntervalSeconds, 1L) * 1000L;
	}

	@Override
	public String getType() {
		return primary.getType();
	}

	private <T> T execute(Supplier<T> primaryCall, Supplier<T> fallbackCall){
		try{
			return primaryCall.get();
		}catch(DataAccessException | IllegalStateException e){
			long now = System.currentTimeMillis();
			long last = lastWarnTime.get();
			if(now - last > warnIntervalMillis && lastWarnTime.compareAndSet(last, now)){
				log.warn("Redis状态存储不可用，已降级为本地状态存储，多实例一致性在此期间不生效: {}", e.getMessage());
			}
			return fallbackCall.get();
		}
	}

	@Override
	public boolean setIfAbsent(String key, String value, long ttlSeconds) {
		return execute(() -> primary.setIfAbsent(key, value, ttlSeconds), () -> fallback.setIfAbsent(key, value, ttlSeconds));
	}

	@Override
	public String get(String key, long ttlSeconds) {
		return execute(() -> primary.get(key, ttlSeconds), () -> fallback.get(key, ttlSeconds));
	}

	@Override
	public void set(String key, String value, long ttlSeconds) {
		execute(() -> {
			primary.set(key, value, ttlSeconds);
			return null;
		}, () -> {
			fallback.set(key, value, ttlSeconds);
			return null;
		});
	}

	@Override
	public boolean delete(String key, long ttlSeconds) {
		return execute(() -> primary.delete(key, ttlSeconds), () -> fallback.delete(key, ttlSeconds));
	}

	@Override
	public boolean exists(String key, long ttlSeconds) {
		return execute(() -> primary.exists(key, ttlSeconds), () -> fallback.exists(key, ttlSeconds));
	}

	@Override
	public boolean expire(String key, long ttlSeconds) {
		return execute(() -> primary.expire(key, ttlSeconds), () -> fallback.expire(key, ttlSeconds));
	}

	@Override
	public long increment(String key, long delta, long ttlSeconds) {
		return execute(() -> primary.increment(key, delta, ttlSeconds), () -> fallback.increment(key, delta, ttlSeconds));
	}

	@Override
	public long deleteByPrefix(String keyPrefix) {
		return execute(() -> primary.deleteByPrefix(keyPrefix), () -> fallback.deleteByPrefix(keyPrefix));
	}
}
