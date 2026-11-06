/**  
* Title DefaultCheckRepeatManagerImpl.java  
* Description  
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.state.DistributedStateStore;
import lombok.Setter;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.CheckRepeatManager;
import com.pddon.framework.easyapi.dto.UniqueToken;

@Setter
public class DefaultCheckRepeatManagerImpl implements CheckRepeatManager {

	/**
	 * 防重码在状态存储中的key前缀
	 */
	private static final String KEY_PREFIX = "repeat-submit:";

	/**
	 * 处理中标记
	 */
	private static final String VALUE_USING = "USING";

	/**
	 * 已提交标记
	 */
	private static final String VALUE_USED = "USED";

	private static final long DEFAULT_TTL_SECONDS = 120L;

	private EasyApiConfig easyApiConfig;
	
	private CacheManager cacheManager;

	/**
	 * 分布式状态存储。未注入时回退到原有CacheManager实现（保持兼容）。
	 * 注入本地实现时与原有行为一致；注入Redis实现时防重码在多实例间共享。
	 */
	private DistributedStateStore stateStore;

	/**
	 * 校验与写入重复提交标记的并发锁分片数量。
	 * 按重复提交码哈希取模，避免同一码的并发请求同时通过校验造成业务重复执行，
	 * 同时避免为每个码维护独立锁对象导致内存增长。
	 * 本地存储模式下用于保证本JVM内的原子性；Redis模式下原子性由SET NX保证。
	 */
	private static final int LOCK_STRIPES = 64;

	private final Object[] checkLocks = new Object[LOCK_STRIPES];

	{
		for(int i = 0; i < LOCK_STRIPES; i++){
			checkLocks[i] = new Object();
		}
	}

	private Object lockOf(String key){
		int hash = key == null ? 0 : key.hashCode();
		return checkLocks[(hash & 0x7fffffff) % LOCK_STRIPES];
	}

	private long ttlSeconds(){
		if(easyApiConfig == null){
			return DEFAULT_TTL_SECONDS;
		}
		long ttl = easyApiConfig.getNoSubmitRepeatTimeoutSeconds();
		return ttl > 0 ? ttl : DEFAULT_TTL_SECONDS;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void check(String key) {
		//检查与写入必须作为一个原子操作：否则两个并发的相同请求可能同时读到无标记状态，
		//都通过校验并各自执行业务，导致防重复提交失效
		long ttl = ttlSeconds();
		if(stateStore != null){
			synchronized (lockOf(key)) {
				if(stateStore.setIfAbsent(KEY_PREFIX + key, VALUE_USING, ttl)){
					return;
				}
				String value = stateStore.get(KEY_PREFIX + key, ttl);
				if(VALUE_USED.equals(value)){
					//数据已提交过，请勿重复提交
					throw new BusinessException(ErrorCodes.UNIQUE_TOKEN_USED);
				}
				//数据正在处理中，请勿重复提交
				throw new BusinessException(ErrorCodes.UNIQUE_TOKEN_USING);
			}
		}
		synchronized (lockOf(key)) {
			UniqueToken token = cacheManager.get(key, UniqueToken.class);
			if(token != null){
				if(token.isSubmit()){
					//数据已提交过，请勿重复提交
					throw new BusinessException(ErrorCodes.UNIQUE_TOKEN_USED);
				}else{
					//数据正在处理中，请勿重复提交
					throw new BusinessException(ErrorCodes.UNIQUE_TOKEN_USING);
				}
			}else{
				//保存token
				token = new UniqueToken();
				token.setCrtTime(System.currentTimeMillis());
				token.setSubmit(false);
				cacheManager.set(key, token, easyApiConfig.getNoSubmitRepeatTimeoutSeconds());
			}
		}
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void release(String key) {
		if(stateStore != null){
			stateStore.delete(KEY_PREFIX + key, ttlSeconds());
			return;
		}
		cacheManager.remove(key);	
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void used(String key) {
		if(stateStore != null){
			long ttl = ttlSeconds();
			if(VALUE_USING.equals(stateStore.get(KEY_PREFIX + key, ttl))){
				stateStore.set(KEY_PREFIX + key, VALUE_USED, ttl);
			}
			return;
		}
		UniqueToken token = cacheManager.get(key, UniqueToken.class);
		if(token != null && !token.isSubmit()){
			token.setSubmit(true);
			cacheManager.set(key, token, easyApiConfig.getNoSubmitRepeatTimeoutSeconds());
		}	
	}

}
