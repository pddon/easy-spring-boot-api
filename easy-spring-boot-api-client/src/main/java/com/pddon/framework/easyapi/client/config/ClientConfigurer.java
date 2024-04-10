package com.pddon.framework.easyapi.client.config;

import com.pddon.framework.easyapi.ApplicationManager;
import com.pddon.framework.easyapi.SecretManager;
import com.pddon.framework.easyapi.client.impl.ClientSecretManagerImpl;
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
    @Bean(name="defaultSignEncryptHandler")
    @ConditionalOnMissingBean(SignEncryptHandler.class)
    public SignEncryptHandler signEncryptHandler() {
        return new Sha1SignEncryptHandler();
    }

    @Bean(name="defaultDataEncryptHandler")
    @ConditionalOnMissingBean(AESDataEncryptHandler.class)
    public DataEncryptHandler dataEncryptHandler(@Autowired SecretManager secretManager) {
        return new AESDataEncryptHandler().setSecretManager(secretManager);
    }

    @Bean(name="defaultSecretManager")
    @ConditionalOnMissingBean(SecretManager.class)
    public SecretManager secretManager(@Autowired ApplicationConfig applicationConfig) {
        ClientSecretManagerImpl secretManager = new ClientSecretManagerImpl(applicationConfig);
        return secretManager;
    }
}
