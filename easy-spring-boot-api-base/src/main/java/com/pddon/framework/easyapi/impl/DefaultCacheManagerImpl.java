/**  
* Title DefaultCacheManagerImpl.java  
* Description  
* @author danyuan
* @date Dec 17, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.impl;

import java.util.Map;

import com.pddon.framework.easyapi.cache.LocalReadCacheContainer;
import com.pddon.framework.easyapi.cache.LocalWriteCacheContainer;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import lombok.Setter;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.dto.CacheManagerState;

@Setter
public class DefaultCacheManagerImpl implements CacheManager {

	private LocalWriteCacheContainer localWriteCacheContainer;
	
	private LocalReadCacheContainer localReadCacheContainer;
	
	/**
	 * @author danyuan
	 */
	@Override
	public <T> void set(String key, T value, Long expireSeconds) {
		this.set(key, value, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}

	@Override
	public <T> void set(String key, T value, Long expireSeconds, CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			localWriteCacheContainer.set(key, value, expireSeconds);
		}else{
			localReadCacheContainer.set(key, value, expireSeconds);
		}		
	}

	@Override
	public <T> void set(String key, T value, Long expireSeconds, Long oldExpireSeconds, CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			localWriteCacheContainer.set(key, value, expireSeconds, oldExpireSeconds);
		}else{
			localReadCacheContainer.set(key, value, expireSeconds, oldExpireSeconds);
		}		
	}
	/**
	 * @author danyuan
	 */
	@Override
	public boolean exists(String key) {
		return this.exists(key, CacheExpireMode.EXPIRE_AFTER_WRITE);		
	}
	
	@Override
	public boolean exists(String key, Long expireSeconds) {
		return this.exists(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);		
	}
	
	@Override
	public boolean exists(String key, CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			return localWriteCacheContainer.exists(key);
		}else{
			return localReadCacheContainer.exists(key);
		}
		
	}
	
	@Override
	public boolean exists(String key, Long expireSeconds, CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			return localWriteCacheContainer.exists(key, expireSeconds);
		}else{
			return localReadCacheContainer.exists(key, expireSeconds);
		}
		
	}

	/**
	 * @author danyuan
	 */
	@Override
	public <T> T get(String key, Class<T> type, Long expireSeconds) {
		return get(key, type, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}
	
	@Override
	public <T> T get(String key, Class<T> type) {
		return get(key, type, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String key, Class<T> type, CacheExpireMode mode) {
		Object content = null;
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			content = localWriteCacheContainer.get(key);
		}else{
			content = localReadCacheContainer.get(key);
		}
		if(content != null && content.getClass().isAssignableFrom(type)){
			return (T)content;
		}
		return null;
	}
	
	@Override
	public Object get(String key, Long expireSeconds, CacheExpireMode mode){
		Object content = null;
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			content = localWriteCacheContainer.get(key, expireSeconds);
		}else{
			content = localReadCacheContainer.get(key, expireSeconds);
		}
		return content;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String key, Class<T> type, Long expireSeconds, CacheExpireMode mode) {
		Object content = get(key, expireSeconds, mode);
		if(content != null && content.getClass().isAssignableFrom(type)){
			return (T)content;
		}
		return null;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void setExpire(String key, Long expireSeconds) {
		this.setExpire(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);		
	}
	
	@Override
	public void setExpire(String key, Long expireSeconds, Long oldExpireSeconds) {
		this.setExpire(key, expireSeconds, oldExpireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);		
	}
	
	@Override
	public void setExpire(String key, Long expireSeconds, CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			localWriteCacheContainer.setExpire(key, expireSeconds);
		}else{
			localReadCacheContainer.setExpire(key, expireSeconds);
		}		
	}
	
	@Override
	public void setExpire(String key, Long expireSeconds, Long oldExpireSeconds, CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			localWriteCacheContainer.setExpire(key, expireSeconds, oldExpireSeconds);
		}else{
			localReadCacheContainer.setExpire(key, expireSeconds, oldExpireSeconds);
		}		
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void remove(String key) {
		this.remove(key, CacheExpireMode.EXPIRE_AFTER_WRITE);		
	}
	
	@Override
	public void remove(String key, Long expireSeconds) {
		this.remove(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);		
	}
	
	@Override
	public void remove(String key, CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			localWriteCacheContainer.remove(key);
		}else{
			localReadCacheContainer.remove(key);
		}
		
	}
	@Override
	public void remove(String key, Long expireSeconds, CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			localWriteCacheContainer.remove(key, expireSeconds);
		}else{
			localReadCacheContainer.remove(key, expireSeconds);
		}
		
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void clear() {
		localWriteCacheContainer.clear(null);
		localReadCacheContainer.clear(null);
	}

	@Override
	public void clear(Long expireSeconds) {
		localWriteCacheContainer.clear(expireSeconds);
		localReadCacheContainer.clear(expireSeconds);
	}
	
	/**
	 * @author danyuan
	 */
	@Override
	public Map<String, CacheManagerState> stats(CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			return localWriteCacheContainer.stats(null);
		}else{
			return localReadCacheContainer.stats(null);
		}
	}

	/**
	 * @author danyuan
	 */
	@Override
	public Map<String, Map<String, Object>> getAllCaches(CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			return localWriteCacheContainer.getCaches(null);
		}else{
			return localReadCacheContainer.getCaches(null);
		}
	}

	/**
	 * @author danyuan
	 */
	@Override
	public Map<String, Object> getCaches(Long expireSeconds, CacheExpireMode mode) {
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			return localWriteCacheContainer.getCaches(expireSeconds).get(expireSeconds);
		}else{
			return localReadCacheContainer.getCaches(expireSeconds).get(expireSeconds);
		}
	}

}
