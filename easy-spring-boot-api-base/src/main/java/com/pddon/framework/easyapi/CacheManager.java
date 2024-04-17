/**  
* Title CacheManager.java  
* Description  缓存管理器
* 用于服务内数据缓存的存取
* @author danyuan
* @date Nov 13, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi;

import java.util.Map;

import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.dto.CacheManagerState;

public interface CacheManager {
	/**
	 * 保存一个缓存，并设置过期时间,默认为写后超时模式
	 * @author danyuan
	 */
	<T> void set(String key, T value, Long expireSeconds);
	/**
	 * 保存一个缓存，并设置过期时间
	 * @author danyuan
	 */
	<T> void set(String key, T value, Long expireSeconds, CacheExpireMode mode);
	
	/**
	 * 保存一个缓存，并设置过期时间
	 * @author danyuan
	 */
	<T> void set(String key, T value, Long expireSeconds, Long oldExpireSeconds, CacheExpireMode mode);
	/**
	 * 是否存在该缓存,默认为写后超时模式
	 * @author danyuan
	 */
	boolean exists(String key);
	/**
	 * 是否存在该缓存,默认为写后超时模式
	 * @author danyuan
	 */
	boolean exists(String key, Long expireSeconds);
	/**
	 * 是否存在该缓存
	 * @author danyuan
	 */
	boolean exists(String key, Long expireSeconds, CacheExpireMode mode);
	/**
	 * 是否存在该缓存
	 * @author danyuan
	 */
	boolean exists(String key, CacheExpireMode mode);
	/**
	 * 获取原始类型的对象
	 * @author danyuan
	 */
	public Object get(String key, Long expireSeconds, CacheExpireMode mode);

	/**
	 * 获取一个缓存,默认为写后超时模式
	 * @author danyuan
	 */
	<T> T get(String key, Class<T> type);
	
	/**
	 * 获取一个缓存,默认为写后超时模式
	 * @author danyuan
	 */
	<T> T get(String key, Class<T> type, Long expireSeconds);
	
	/**
	 * 获取一个缓存
	 * @author danyuan
	 */
	<T> T get(String key, Class<T> type, CacheExpireMode mode);
	
	/**
	 * 获取一个缓存
	 * @author danyuan
	 */
	<T> T get(String key, Class<T> type, Long expireSeconds, CacheExpireMode mode);
	
	/**
	 * 重新设置一个缓存的超时时间,默认为写后超时模式
	 * @author danyuan
	 */
	void setExpire(String key, Long expireSeconds);
	/**
	 * 重新设置一个缓存的超时时间,默认为写后超时模式
	 * @author danyuan
	 */
	void setExpire(String key, Long expireSeconds, Long oldExpireSeconds);
	/**
	 * 重新设置一个缓存的超时时间
	 * @author danyuan
	 */
	void setExpire(String key, Long expireSeconds, Long oldExpireSeconds, CacheExpireMode mode);
	/**
	 * 重新设置一个缓存的超时时间
	 * @author danyuan
	 */
	void setExpire(String key, Long expireSeconds, CacheExpireMode mode);
	/**
	 * 删除缓存,默认为写后超时模式
	 * @author danyuan
	 */
	void remove(String key);
	/**
	 * 删除缓存,默认为写后超时模式
	 * @author danyuan
	 */
	void remove(String key, Long expireSeconds);
	/**
	 * 删除缓存
	 * @author danyuan
	 */
	void remove(String key, Long expireSeconds, CacheExpireMode mode);
	/**
	 * 删除缓存
	 * @author danyuan
	 */
	void remove(String key, CacheExpireMode mode);
	/**
	 * 清空所有缓存
	 * @author danyuan
	 */
	void clear();
	/**
	 * 清空所有缓存
	 * @author danyuan
	 */
	void clear(Long expireSeconds);
	/**
	 * 缓存管理器统计数据
	 * @author danyuan
	 */
	Map<String, CacheManagerState> stats(CacheExpireMode mode);
	/**
	 * 查询所有缓存信息
	 * @author danyuan
	 */
	Map<String, Map<String, Object>> getAllCaches(CacheExpireMode mode);
	/**
	 * 查询某个时间失效的所有缓存信息
	 * @author danyuan
	 */
	Map<String, Object> getCaches(Long expireSeconds, CacheExpireMode mode);
	
}
