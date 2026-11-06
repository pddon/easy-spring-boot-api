/**
* Title SignReplayProtector.java
* Description 验签防重放校验：时间窗校验 + nonce一次性校验
* @author pddon
* @date 2026-11-05
* @version 1.0.0
* site: pddon.cn
*/
package com.pddon.framework.easyapi.security;

import org.springframework.stereotype.Component;

import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.state.DistributedStateStore;
import lombok.extern.slf4j.Slf4j;

/**
 * 验签防重放校验器。
 * 默认关闭（easyapi.sign.replay-protection.enabled=false），关闭时不改变原有验签行为。
 * 开启后：
 * 1、时间戳必须在允许的时间窗内；
 * 2、nonce必须由客户端传递、参与签名且在nonce缓存有效期内只能使用一次。
 * nonce通过统一状态存储（DistributedStateStore）消费：未接入Redis时为单JVM一次性校验，
 * 接入Redis后跨实例保证同一nonce只能消费一次（SET NX原子操作）。
 */
@Component
@Slf4j
public class SignReplayProtector {

	private static final long DEFAULT_TIMESTAMP_WINDOW_SECONDS = 300L;
	private static final long DEFAULT_NONCE_TTL_SECONDS = 300L;

	/**
	 * nonce在状态存储中的key前缀
	 */
	private static final String NONCE_KEY_PREFIX = "sign:nonce:";

	private final EasyApiConfig easyApiConfig;

	private final DistributedStateStore stateStore;

	public SignReplayProtector(EasyApiConfig easyApiConfig, DistributedStateStore stateStore) {
		this.easyApiConfig = easyApiConfig;
		this.stateStore = stateStore;
	}

	private long nonceTtlSeconds(){
		long ttl = easyApiConfig.getSign().getReplayProtection().getNonceTtlSeconds();
		return ttl > 0 ? ttl : DEFAULT_NONCE_TTL_SECONDS;
	}

	public boolean isEnabled(){
		return easyApiConfig.getSign().getReplayProtection().isEnabled();
	}

	public String getNonceParam(){
		String nonceParam = easyApiConfig.getSign().getReplayProtection().getNonceParam();
		return nonceParam == null || nonceParam.trim().isEmpty() ? "nonce" : nonceParam;
	}

	/**
	 * 校验时间戳格式与时间窗
	 * @param timestamp 客户端时间戳（毫秒）
	 * @param timestampParamName 时间戳参数名，用于错误信息定位
	 */
	public void checkTimestamp(String timestamp, String timestampParamName){
		long windowSeconds = easyApiConfig.getSign().getReplayProtection().getTimestampWindowSeconds();
		if(windowSeconds <= 0){
			windowSeconds = DEFAULT_TIMESTAMP_WINDOW_SECONDS;
		}
		long timestampValue;
		try{
			timestampValue = Long.parseLong(timestamp == null ? "" : timestamp.trim());
		}catch(NumberFormatException e){
			throw new BusinessException(ErrorCodes.ERROR_SIGN).setParam(timestampParamName, timestamp);
		}
		long deviation = Math.abs(System.currentTimeMillis() - timestampValue);
		if(deviation > windowSeconds * 1000L){
			throw new BusinessException(ErrorCodes.ERROR_SIGN).setParam(timestampParamName, timestamp);
		}
	}

	/**
	 * 在验签前校验nonce是否合法（非空、长度受限）
	 */
	public void requireValidNonce(String nonce){
		String nonceParam = getNonceParam();
		int maxLength = easyApiConfig.getSign().getReplayProtection().getNonceMaxLength();
		if(maxLength <= 0){
			maxLength = 128;
		}
		if(nonce == null || nonce.trim().isEmpty() || nonce.length() > maxLength){
			throw new BusinessException(ErrorCodes.ERROR_SIGN).setParam(nonceParam, nonce);
		}
	}

	/**
	 * 验签通过后消费nonce，重复使用视为重放攻击。
	 * 状态存储的setIfAbsent（本地为分片锁+判断写入，Redis为SET NX）保证同一nonce并发请求只有一个成功。
	 */
	public void consumeNonce(String nonce){
		if(nonce == null || nonce.trim().isEmpty()){
			throw new BusinessException(ErrorCodes.ERROR_SIGN).setParam(getNonceParam(), nonce);
		}
		if(!stateStore.setIfAbsent(NONCE_KEY_PREFIX + nonce, "1", nonceTtlSeconds())){
			if(log.isDebugEnabled()){
				log.debug("检测到验签重放，nonce已使用:[{}]", nonce);
			}
			throw new BusinessException(ErrorCodes.REPLAY_ATTACK).setParam(nonce);
		}
	}
}
