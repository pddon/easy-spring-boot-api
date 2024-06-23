/**  
* Title ClientAESDataEncryptHandler.java
* Description  默认采用渠道对称秘钥进行AES128加解密
* @author danyuan
* @date Nov 15, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.impl;

import com.pddon.framework.easyapi.SecretManager;
import com.pddon.framework.easyapi.client.ClientDataEncryptHandler;
import com.pddon.framework.easyapi.client.ClientSecretManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.SecretInfo;
import com.pddon.framework.easyapi.encrypt.DataEncryptHandler;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.EncryptUtils;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(chain = true)
public class ClientAESDataEncryptHandler implements ClientDataEncryptHandler {

	private ClientSecretManager clientSecretManager;
	/**
	 * @author danyuan
	 * @throws Exception 
	 */
	@Override
	public String encrypt(String appId, String channelId, String content) throws Throwable {
		SecretInfo secret = clientSecretManager.load(channelId,
				appId,
				null);
		if(secret != null){
			return EncryptUtils.encodeAES128(secret.getSecret(), content);
		}
		throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam("secret_key");
	}

	/**
	 * @author danyuan
	 */
	@Override
	public String decrypt(String appId, String channelId, String content) throws Throwable {
		SecretInfo secret = clientSecretManager.load(channelId,
				appId,
				null);
		if(secret != null){
			return EncryptUtils.decodeAES128(secret.getSecret(), content);
		}		
		throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam("secret_key");
	}

}
