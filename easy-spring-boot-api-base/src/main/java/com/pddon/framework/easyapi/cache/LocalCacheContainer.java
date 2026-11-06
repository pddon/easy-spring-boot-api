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
import com.pddon.framework.easyapi.properties.LocalCacheConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;

import com.pddon.framework.easyapi.dto.CacheManagerState;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

public class LocalCacheContainer {
	/**
	 * 单个缓存管理器默认最多能缓存多少key
	 */
	private static final Long DEFAULT_MAX_CACHE_SIZE = 100000L;
	/**
	 * 默认最多允许开启多少个缓存
	 */
	private static final Long DEFAULT_MAX_CACHE_NUM = 2000L;
	/**
	 * 默认缓存管理器超时时间,3分钟
	 */
	private static final Long DEFAULT_CACHE_EXPIRE_SECONDS = 180L;
	/**
	 * 缓存模式
	 */
	private CacheExpireMode mode;
	/**
	 * 解析lock-free配置：容器中不存在LocalCacheConfig时保持默认的false
	 */
	protected static boolean resolveLockFree(ObjectProvider<LocalCacheConfig> configProvider){
		LocalCacheConfig config = configProvider == null ? null : configProvider.getIfAvailable();
		return config != null && config.isLockFree();
	}

	/**
	 * 解析easyapi.local-cache.max-cache-size配置，缺失时使用默认值100000
	 */
	protected static Long resolveMaxCacheSize(ObjectProvider<LocalCacheConfig> configProvider){
		LocalCacheConfig config = configProvider == null ? null : configProvider.getIfAvailable();
		return config == null ? DEFAULT_MAX_CACHE_SIZE : config.getMaxCacheSize();
	}

	/**
	 * 解析easyapi.local-cache.max-cache-num配置，缺失时使用默认值2000
	 */
	protected static Long resolveMaxCacheNum(ObjectProvider<LocalCacheConfig> configProvider){
		LocalCacheConfig config = configProvider == null ? null : configProvider.getIfAvailable();
		return config == null ? DEFAULT_MAX_CACHE_NUM : config.getMaxCacheNum();
	}

	/**
	 * 是否关闭缓存管理器的全局锁。
	 * 默认false，保持原有行为；开启后set/remove/clear不再串行化，
	 * 仅在创建新的缓存管理器时使用一次锁（双检锁保证幂等）。
	 */
	private final boolean lockFree;
	/**
	 * 单个过期时间桶的最大key数量（可配置，默认100000）
	 */
	private final long maxCacheSize;
	/**
	 * 最多允许创建的过期时间桶数量（可配置，默认2000）
	 */
	private final long maxCacheNum;
	
	private ConcurrentHashMap<String, Cache<String, Object>> cacheMap = new ConcurrentHashMap<>();
	private Lock lock = new ReentrantLock();
	
	public LocalCacheContainer(CacheExpireMode mode) {//初始化默认缓存管理器
		this(mode, false);
	}

	/**
	 * @param mode 缓存过期模式
	 * @param lockFree 是否关闭全局锁（true时读写路径不再加全局锁）
	 */
	public LocalCacheContainer(CacheExpireMode mode, boolean lockFree) {
		this(mode, lockFree, DEFAULT_MAX_CACHE_SIZE, DEFAULT_MAX_CACHE_NUM);
	}

	/**
	 * @param mode 缓存过期模式
	 * @param lockFree 是否关闭全局锁
	 * @param maxCacheSize 单个过期时间桶的最大key数量，非正数时回退默认值
	 * @param maxCacheNum 最多创建的过期时间桶数量，非正数时回退默认值
	 */
	public LocalCacheContainer(CacheExpireMode mode, boolean lockFree, Long maxCacheSize, Long maxCacheNum) {
		this.mode = mode;
		this.lockFree = lockFree;
		this.maxCacheSize = (maxCacheSize == null || maxCacheSize <= 0) ? DEFAULT_MAX_CACHE_SIZE : maxCacheSize;
		this.maxCacheNum = (maxCacheNum == null || maxCacheNum <= 0) ? DEFAULT_MAX_CACHE_NUM : maxCacheNum;
		cacheMap = new ConcurrentHashMap<>();
		Cache<String, Object> cacheContainer = null;
		if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
			cacheContainer = CacheBuilder.newBuilder()
	                .maximumSize(this.maxCacheSize)
	                .expireAfterWrite(DEFAULT_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS)//最后一次访问后的一段时间移出
	                .recordStats()//开启统计功能
	                .build();
		}else{
			cacheContainer = CacheBuilder.newBuilder()
	                .maximumSize(this.maxCacheSize)
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

        //一次查找即可命中已存在的缓存管理器
        cacheContainer = cacheMap.get(mapKey);
        if(cacheContainer != null){
            return cacheContainer;
        }

        if(cacheMap.size() >= maxCacheNum){
        	//缓存管理器已经大于上限值，返回默认时效的缓存管理器
        	return cacheMap.get(String.valueOf(DEFAULT_CACHE_EXPIRE_SECONDS));
        }
        
        try {
            lock.lock();   
            //双重检查，避免并发场景下重复创建同一个缓存管理器
            cacheContainer = cacheMap.get(mapKey);
            if(cacheContainer != null){
            	return cacheContainer;
            }
            //锁内再次校验上限，避免并发创建时突破maxCacheNum
            if(cacheMap.size() >= maxCacheNum){
            	return cacheMap.get(String.valueOf(DEFAULT_CACHE_EXPIRE_SECONDS));
            }
            if(CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode)){
            	cacheContainer = CacheBuilder.newBuilder()
                        .maximumSize(maxCacheSize)
                        .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)//最后一次访问后的一段时间移出
                        .recordStats()//开启统计功能
                        .build();

    		}else{
    			cacheContainer = CacheBuilder.newBuilder()
                        .maximumSize(maxCacheSize)
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

	/**
	 * 读/写路径的全局锁：lockFree=true时跳过，利用ConcurrentHashMap与Guava Cache自身的线程安全
	 */
	private void acquireGlobalLock(){
		if(!lockFree){
			lock.lock();
		}
	}

	private void releaseGlobalLock(){
		if(!lockFree){
			lock.unlock();
		}
	}

	/**
	 * 无锁模式写入：先写入目标过期桶，再清除其它桶中的同名key，
	 * 避免并发set同一key到不同过期桶时长期残留多份值导致读到旧值
	 */
	private void putAndCleanup(String key, Object content, Long seconds){
		Cache<String, Object> cacheContainer = getCache(seconds);
		cacheContainer.put(key, content);
		String keepMapKey = String.valueOf(seconds);
		for(Map.Entry<String, Cache<String,Object>> entry : cacheMap.entrySet()){
			if(!keepMapKey.equals(entry.getKey())){
				entry.getValue().invalidate(key);
			}
		}
	}
	
	public void set(String key, Object content, Long seconds){
		try {
            acquireGlobalLock(); 
            if(lockFree){
            	putAndCleanup(key, content, seconds);
            }else{
            	//删除旧key
            	remove(key);
            	//保存新key
        		Cache<String, Object> cacheContainer = getCache(seconds);
        		cacheContainer.put(key, content);
            }
		} finally {
            releaseGlobalLock();
        }		
	}
	
	public void set(String key, Object content, Long expireSeconds, Long oldExpireSeconds){
		try {
            acquireGlobalLock(); 
            if(lockFree){
            	putAndCleanup(key, content, expireSeconds);
            }else{
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
            }
		} finally {
            releaseGlobalLock();
        }		
	}
	
	public void setExpire(String key, Long seconds){
		Object content = get(key);
		if(content == null){
			return ;
		}
		try {
            acquireGlobalLock();             
            if(lockFree){
            	putAndCleanup(key, content, seconds);
            }else{
            	//删除旧key
                remove(key);
                //保存新key
        		Cache<String, Object> cacheContainer = getCache(seconds);
        		cacheContainer.put(key, content);
            }
		} finally {
            releaseGlobalLock();
        }		
	}
	
	public void setExpire(String key, Long seconds, Long oldExpireSeconds){
		Object content = get(key);
		if(content == null){
			return ;
		}
		try {
            acquireGlobalLock();   
            if(lockFree){
            	putAndCleanup(key, content, seconds);
            }else{
            	if(oldExpireSeconds != null){
                	remove(key, oldExpireSeconds);
                }else{
                	remove(key);
                }
                
                //保存新key
        		Cache<String, Object> cacheContainer = getCache(seconds);
        		cacheContainer.put(key, content);
            }
		} finally {
            releaseGlobalLock();
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
            acquireGlobalLock(); 
			if(lockFree){
				//无锁模式下并发set同一key时，key可能短暂存在于多个过期桶，
				//这里清除所有桶中的副本，保证remove后不会读到旧值
				for(Cache<String,Object> cache : cacheMap.values()){
					cache.invalidate(key);
				}
			}else{
				//直接使用定位到的缓存管理器，避免缓存管理器数量达到上限时
				//getCache回退到默认管理器导致目标key未被真正删除
				Cache<String, Object> cacheContainer = locateCache(key);
				if(cacheContainer != null){
					cacheContainer.invalidate(key);
				}
			}
		} finally {
            releaseGlobalLock();
        }		
	}
	
	public void remove(String key, Long expireSeconds){
		if(expireSeconds != null){
			try {
	            acquireGlobalLock(); 
	            Cache<String, Object> cacheContainer = getCache(expireSeconds);
				cacheContainer.invalidate(key);
			} finally {
	            releaseGlobalLock();
	        }
		}else{
			remove(key);
		}				
	}
	
	public void clear(Long expireSeconds){
		try {
            acquireGlobalLock(); 
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
            releaseGlobalLock();
        }	
		
	}
	
	public boolean exists(String key){
		return get(key) != null;
	}
	
	public boolean exists(String key, Long expireSeconds){
		return get(key, expireSeconds) != null;
	}
	
	private Cache<String, Object> locateCache(String key){
		for(Cache<String,Object> cache : cacheMap.values()){
			if(cache.getIfPresent(key) != null){
				return cache;
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
