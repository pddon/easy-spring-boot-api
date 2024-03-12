/**  
* Title DefaultSecretManagerImpl.java  
* Description  通过读取配置文件信息进行渠道应用安全控制,暂时支持渠道、应用级别安全控制
* @author danyuan
* @date Dec 15, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.ApplicationManager;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.pddon.framework.easyapi.SecretManager;
import com.pddon.framework.easyapi.dto.ApiPermissionDto;
import com.pddon.framework.easyapi.dto.SecretInfo;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultSecretManagerImpl implements SecretManager {



	private ApplicationManager applicationManager;

	/**
	 * @author danyuan
	 */
	@Override
	public SecretInfo load(String channelId, String appId, String secretId,
			String userId, String sessionId) {
		ApiPermissionDto permission = null;
		if(appId != null){
			permission = applicationManager.getAppPermission(appId);
		}
		if(permission == null){
			permission = applicationManager.getChannelPermission(channelId);
		}
		if(permission != null){
			SecretInfo secret = new SecretInfo();
			secret.setKeyPair(permission.getKeyPair());
			secret.setSecret(permission.getSecret());
			return secret;
		}
		return null;
	}
	

}
