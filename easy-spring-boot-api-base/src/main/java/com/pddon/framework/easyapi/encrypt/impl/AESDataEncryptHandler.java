/**  
* Title AESDataEncryptHandler.java  
* Description  默认采用渠道对称秘钥进行AES128加解密
* @author danyuan
* @date Nov 15, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.encrypt.impl;

import com.pddon.framework.easyapi.encrypt.DataEncryptHandler;
import com.pddon.framework.easyapi.exception.BusinessException;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.pddon.framework.easyapi.SecretManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.SecretInfo;
import com.pddon.framework.easyapi.utils.EncryptUtils;

@Setter
@Accessors(chain = true)
public class AESDataEncryptHandler implements DataEncryptHandler {

	private SecretManager secretManager;
	/**
	 * @author danyuan
	 * @throws Exception 
	 */
	@Override
	public String encrypt(String appId, String channelId, String userId, String content) throws Throwable {
		SecretInfo secret = secretManager.load(RequestContext.getContext().getChannelId(), 				
				RequestContext.getContext().getAppId(), 
				RequestContext.getContext().getSecretId(), 
				RequestContext.getContext().getUserId(), 
				RequestContext.getContext().getSessionId());
		if(secret != null){
			return EncryptUtils.encodeAES128(secret.getSecret(), content);
		}
		throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam("secret_key");
	}

	/**
	 * @author danyuan
	 */
	@Override
	public String decrypt(String appId, String channelId, String userId, String content) throws Throwable {
		SecretInfo secret = secretManager.load(RequestContext.getContext().getChannelId(), 				
				RequestContext.getContext().getAppId(), 
				RequestContext.getContext().getSecretId(), 
				RequestContext.getContext().getUserId(), 
				RequestContext.getContext().getSessionId());
		if(secret != null){
			return EncryptUtils.decodeAES128(secret.getSecret(), content);
		}		
		throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam("secret_key");
	}

}
