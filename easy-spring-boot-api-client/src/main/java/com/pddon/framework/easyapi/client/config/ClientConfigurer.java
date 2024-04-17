package com.pddon.framework.easyapi.client.config;

import com.pddon.framework.easyapi.ApplicationManager;
import com.pddon.framework.easyapi.SecretManager;
import com.pddon.framework.easyapi.client.ApiClient;
import com.pddon.framework.easyapi.client.ClientDataEncryptHandler;
import com.pddon.framework.easyapi.client.ClientSecretManager;
import com.pddon.framework.easyapi.client.ClientSignEncryptHandler;
import com.pddon.framework.easyapi.client.impl.ClientAESDataEncryptHandler;
import com.pddon.framework.easyapi.client.impl.ClientSecretManagerImpl;
import com.pddon.framework.easyapi.client.impl.ClientSha1SignEncryptHandler;
import com.pddon.framework.easyapi.encrypt.DataEncryptHandler;
import com.pddon.framework.easyapi.encrypt.SignEncryptHandler;
import com.pddon.framework.easyapi.encrypt.impl.AESDataEncryptHandler;
import com.pddon.framework.easyapi.encrypt.impl.Sha1SignEncryptHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: ClientConfigurer
 * @Description:
 * @Author: Allen
 * @Date: 2024-03-08 13:25
 * @Addr: https://pddon.cn
 */
@Configuration
@ComponentScan("com.pddon.framework.easyapi.client")
public class ClientConfigurer {
    @Bean
    @ConditionalOnMissingBean(ApiClient.class)
    public ApiClient apiClient(@Autowired ApplicationConfig applicationConfig){
        return ApiClient.newInstance(applicationConfig);
    }

    @Bean(name="clientSignEncryptHandler")
    @ConditionalOnMissingBean(ClientSignEncryptHandler.class)
    public ClientSignEncryptHandler clientSignEncryptHandler() {
        return new ClientSha1SignEncryptHandler();
    }

    @Bean(name="clientDataEncryptHandler")
    @ConditionalOnMissingBean(ClientAESDataEncryptHandler.class)
    public ClientDataEncryptHandler clientSignEncryptHandler(@Autowired ClientSecretManager clientSecretManager) {
        return new ClientAESDataEncryptHandler().setClientSecretManager(clientSecretManager);
    }

    @Bean(name="clientSecretManager")
    @ConditionalOnMissingBean(ClientSecretManagerImpl.class)
    public ClientSecretManager clientSecretManager() {
        ClientSecretManagerImpl secretManager = new ClientSecretManagerImpl();
        return secretManager;
    }
}
