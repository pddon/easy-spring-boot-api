package com.pddon.framework.easyapi.client.impl;

import com.pddon.framework.easyapi.SecretManager;
import com.pddon.framework.easyapi.client.ClientSecretManager;
import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.dto.SecretInfo;
import com.pddon.framework.easyapi.dto.SecretKeyPair;
import com.pddon.framework.easyapi.exception.BusinessException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: ClientSecretManagerImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-10 17:06
 * @Addr: https://pddon.cn
 */
public class ClientSecretManagerImpl implements ClientSecretManager {
    private static final Map<String, ApplicationConfig> applicationConfigs = new HashMap<>();
    @Override
    public SecretInfo load(String channelId, String appId, String secretId) {
        ApplicationConfig applicationConfig = applicationConfigs.get(appId);
        if(applicationConfig == null){
            throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam("applicationConfig");
        }
        SecretInfo secretInfo = new SecretInfo();
        secretInfo.setSecret(applicationConfig.getSecret());
        secretInfo.setKeyPair(new SecretKeyPair(applicationConfig.getPrivateSecret(), applicationConfig.getPublicSecret()));
        return secretInfo;
    }

    public static void addApplicationConfig(ApplicationConfig applicationConfig){
        applicationConfigs.put(applicationConfig.getAppId(), applicationConfig);
    }
}
