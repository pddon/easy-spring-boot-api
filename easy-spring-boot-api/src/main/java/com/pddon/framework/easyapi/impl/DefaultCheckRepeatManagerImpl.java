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
import lombok.Setter;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.CheckRepeatManager;
import com.pddon.framework.easyapi.dto.UniqueToken;

@Setter
public class DefaultCheckRepeatManagerImpl implements CheckRepeatManager {

	private EasyApiConfig easyApiConfig;
	
	private CacheManager cacheManager;

	/**
	 * @author danyuan
	 */
	@Override
	public void check(String key) {
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

	/**
	 * @author danyuan
	 */
	@Override
	public void release(String key) {
		cacheManager.remove(key);	
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void used(String key) {
		UniqueToken token = cacheManager.get(key, UniqueToken.class);
		if(token != null && !token.isSubmit()){
			token.setSubmit(true);
			cacheManager.set(key, token, easyApiConfig.getNoSubmitRepeatTimeoutSeconds());
		}	
	}

}
