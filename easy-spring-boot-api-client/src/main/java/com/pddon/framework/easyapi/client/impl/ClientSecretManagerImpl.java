package com.pddon.framework.easyapi.client.impl;

import com.pddon.framework.easyapi.SecretManager;
import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.dto.SecretInfo;
import com.pddon.framework.easyapi.dto.SecretKeyPair;

/**
 * @ClassName: ClientSecretManagerImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-10 17:06
 * @Addr: https://pddon.cn
 */
public class ClientSecretManagerImpl implements SecretManager {
    private ApplicationConfig applicationConfig;
    public ClientSecretManagerImpl(ApplicationConfig applicationConfig){
        this.applicationConfig = applicationConfig;
    }
    @Override
    public SecretInfo load(String channelId, String appId, String secretId, String userId, String sessionId) {
        SecretInfo secretInfo = new SecretInfo();
        secretInfo.setSecret(applicationConfig.getSecret());
        secretInfo.setKeyPair(new SecretKeyPair(applicationConfig.getPrivateSecret(), applicationConfig.getPublicSecret()));
        return secretInfo;
    }
}
