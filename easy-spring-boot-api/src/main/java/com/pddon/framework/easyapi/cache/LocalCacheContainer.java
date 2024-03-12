/**  
* Title LocalCacheContainer.java  
* Description  本地缓存管理器，以失效时间为key进行管理
* @author danyuan
* @date Dec 17, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.pddon.framework.easyapi.consts.CacheExpireMode;
import org.springframework.beans.BeanUtils;

import com.pddon.framework.easyapi.dto.CacheManagerState;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

public class LocalCacheContainer {
	/**
	 * 单个缓存管理器最多能缓存多少key
	 */
	private static final Long MAX_CACHE_SIZE = 100000L;
	/**
	 * 最多允许开启多少个缓存
	 */
	private static final Long MAX_CACHE_NUM = 2000L;
	/**
	 * 默认缓存管理器超时时间,3分钟
	 */
	private static final Long DEFAULT_CACHE_EXPIRE_SECONDS = 180L;
	/**
	 * 缓存模式
	 */
	private CacheExpireMode mode;
	
	private ConcurrentHashMap<String, Cache<String, Object>> cacheMap = new ConcurrentHashMap<>();
	private Lock lock = new ReentrantLock();
	
	public LocalCacheContainer(CacheExpireMode mode) {//初始化默认缓存管理器
		this.mode = mode;
		cacheMap = new ConcurrentHashMap<>();
		Cache<String, Object> cacheContainer = null;
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			cacheContainer = CacheBuilder.newBuilder()
	                .maximumSize(MAX_CACHE_SIZE)
	                .expireAfterWrite(DEFAULT_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS)//最后一次访问后的一段时间移出
	                .recordStats()//开启统计功能
	                .build();
		}else{
			cacheContainer = CacheBuilder.newBuilder()
	                .maximumSize(MAX_CACHE_SIZE)
	                .expireAfterAccess(DEFAULT_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS)//最后一次访问后的一段时间移出
	                .recordStats()//开启统计功能
	                .build();
		}
		
		cacheMap.put(String.valueOf(DEFAULT_CACHE_EXPIRE_SECONDS), cacheContainer);
	};
	
	private Cache<String, Object> getCache(Long expireSeconds){
		Cache<String, Object> cacheContainer = null;
        if (expireSeconds == null) {
            return cacheContainer;
        }

        String mapKey = String.valueOf(expireSeconds);

        if (cacheMap.containsKey(mapKey) == true) {
            cacheContainer = cacheMap.get(mapKey);
            return cacheContainer;
        }

        if(cacheMap.size() >= MAX_CACHE_NUM){
        	//缓存管理器已经大于上限值，返回默认时效的缓存管理器
        	return cacheMap.get(String.valueOf(DEFAULT_CACHE_EXPIRE_SECONDS));
        }
        
        try {
            lock.lock();   
            if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
            	cacheContainer = CacheBuilder.newBuilder()
                        .maximumSize(MAX_CACHE_SIZE)
                        .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)//最后一次访问后的一段时间移出
                        .recordStats()//开启统计功能
                        .build();

    		}else{
    			cacheContainer = CacheBuilder.newBuilder()
                        .maximumSize(MAX_CACHE_SIZE)
                        .expireAfterAccess(expireSeconds, TimeUnit.SECONDS)//最后一次访问后的一段时间移出
                        .recordStats()//开启统计功能
                        .build();

    		}            
            cacheMap.put(mapKey, cacheContainer);

        } finally {
            lock.unlock();
        }

        return cacheContainer;
	}
	
	public void set(String key, Object content, Long seconds){
		try {
            lock.lock(); 
            //删除旧key
            remove(key);
            //保存新key
    		Cache<String, Object> cacheContainer = getCache(seconds);
    		cacheContainer.put(key, content);
		} finally {
            lock.unlock();
        }		
	}
	
	public void set(String key, Object content, Long expireSeconds, Long oldExpireSeconds){
		try {
            lock.lock(); 
            if(oldExpireSeconds != null){
            	//删除旧key
                remove(key, oldExpireSeconds);
            }else{
            	//删除旧key
                remove(key);
            }            
            //保存新key
    		Cache<String, Object> cacheContainer = getCache(expireSeconds);
    		cacheContainer.put(key, content);
		} finally {
            lock.unlock();
        }		
	}
	
	public void setExpire(String key, Long seconds){
		Object content = get(key);
		if(content == null){
			return ;
		}
		try {
            lock.lock();             
            //删除旧key
            remove(key);
            //保存新key
    		Cache<String, Object> cacheContainer = getCache(seconds);
    		cacheContainer.put(key, content);
		} finally {
            lock.unlock();
        }		
	}
	
	public void setExpire(String key, Long seconds, Long oldExpireSeconds){
		Object content = get(key);
		if(content == null){
			return ;
		}
		try {
            lock.lock();   
            if(oldExpireSeconds != null){
            	remove(key, oldExpireSeconds);
            }else{
            	remove(key);
            }
            
            //保存新key
    		Cache<String, Object> cacheContainer = getCache(seconds);
    		cacheContainer.put(key, content);
		} finally {
            lock.unlock();
        }		
	}
	
	public Object get(String key){
		for(Cache<String,Object> cache : cacheMap.values()){
			Object content = cache.getIfPresent(key);
			if(content != null){
				return content;
			}			
		}
		return null;
	}
	
	public Object get(String key, Long expireSeconds){
		if(expireSeconds == null){
			return get(key);
		}
		Cache<String, Object> cacheContainer = getCache(expireSeconds);		
		Object content = cacheContainer.getIfPresent(key);
		if(content != null){
			return content;
		}
		return null;
	}
	
	public void remove(String key){
		try {
            lock.lock(); 
			String existsLocate = locate(key);
			if(existsLocate != null){
				Cache<String, Object> cacheContainer = getCache(Long.valueOf(existsLocate));
				cacheContainer.invalidate(key);
			}
		} finally {
            lock.unlock();
        }		
	}
	
	public void remove(String key, Long expireSeconds){
		if(expireSeconds != null){
			try {
	            lock.lock(); 
	            Cache<String, Object> cacheContainer = getCache(expireSeconds);
				cacheContainer.invalidate(key);
			} finally {
	            lock.unlock();
	        }
		}else{
			remove(key);
		}				
	}
	
	public void clear(Long expireSeconds){
		try {
            lock.lock(); 
            if(expireSeconds == null){
    			//清空所有缓存
    			cacheMap.forEach((seconds, cache) -> {
    				cache.invalidateAll();
    			});
    		}else{
    			Cache<String, Object> cacheContainer = getCache(expireSeconds);
    			cacheContainer.invalidateAll();
    		}
		} finally {
            lock.unlock();
        }	
		
	}
	
	public boolean exists(String key){
		return get(key) != null;
	}
	
	public boolean exists(String key, Long expireSeconds){
		return get(key, expireSeconds) != null;
	}
	
	private String locate(String key){
		for(Map.Entry<String, Cache<String,Object>> entry : cacheMap.entrySet()){
			Object content = entry.getValue().getIfPresent(key);
			if(content != null){
				return entry.getKey();
			}
		}
		return null;
	}
	
	public Map<String, CacheManagerState> stats(Long expireSeconds) {
		Map<String, CacheManagerState> map = new HashMap<>();
		Cache<String, Object> cacheContainer = null;
		CacheStats stats = null;
		CacheManagerState state = null;
		if(expireSeconds != null){
			cacheContainer = getCache(expireSeconds);
			stats = cacheContainer.stats();
			state = new CacheManagerState();
			BeanUtils.copyProperties(stats, state);
			state.setCacheName("cacheManager:"+expireSeconds);
			map.put(String.valueOf(expireSeconds), state);
		}else{
			for(Map.Entry<String, Cache<String,Object>> entry : cacheMap.entrySet()){
				cacheContainer = entry.getValue();
				stats = cacheContainer.stats();
				state = new CacheManagerState();
				BeanUtils.copyProperties(stats, state);
				state.setCacheName("cacheManager:"+entry.getKey());
				map.put(String.valueOf(entry.getKey()), state);
			}
		}
		return map;
	}
	
	public Map<String, Map<String, Object>> getCaches(Long expireSeconds){
		Map<String, Map<String, Object>> map = new HashMap<>();
		Cache<String, Object> cacheContainer = null;		
		if(expireSeconds != null){
			cacheContainer = getCache(expireSeconds);			
			map.put(String.valueOf(expireSeconds), cacheContainer.asMap());
		}else{
			for(Map.Entry<String, Cache<String,Object>> entry : cacheMap.entrySet()){
				cacheContainer = entry.getValue();
				map.put(this.mode.name() + ":" + String.valueOf(entry.getKey()), cacheContainer.asMap());
			}
		}
		return map;
	}
	
}
