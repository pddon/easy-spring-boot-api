/**  
 * Title EasyApiBeanConfigurer.java
 * Description  组装和生产管理器
 * @author danyuan
 * @date Oct 31, 2020
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.config;


import java.util.Locale;

import com.pddon.framework.easyapi.encrypt.DataEncryptHandler;
import com.pddon.framework.easyapi.encrypt.SignEncryptHandler;
import com.pddon.framework.easyapi.encrypt.impl.AESDataEncryptHandler;
import com.pddon.framework.easyapi.encrypt.impl.Sha1SignEncryptHandler;
import com.pddon.framework.easyapi.properties.ChannelConfigProperties;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pddon.framework.easyapi.ApplicationManager;
import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.CheckRepeatManager;
import com.pddon.framework.easyapi.InvokeApiLogManager;
import com.pddon.framework.easyapi.InvokeTimesManager;
import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.SecretManager;
import com.pddon.framework.easyapi.SessionManager;
import com.pddon.framework.easyapi.SignManager;
import com.pddon.framework.easyapi.cache.LocalReadCacheContainer;
import com.pddon.framework.easyapi.cache.LocalWriteCacheContainer;
import com.pddon.framework.easyapi.impl.DefaultApplicationManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultCacheManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultCheckRepeatManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultInvokeApiLogManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultInvokeTimesManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultLanguageTranslateManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultSecretManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultSessionManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultSignManagerImpl;


@Configuration
@ConditionalOnProperty(name={"easyapi.enable"},havingValue = "true")
@AutoConfigureAfter(EasyApiWebConfigurer.class)
public class EasyApiBeanConfigurer {

	@Bean(name="defaultApplicationManager")
	@ConditionalOnMissingBean(ApplicationManager.class)
	public ApplicationManager applicationManager(@Autowired ChannelConfigProperties channelConfigProperties, @Autowired SystemParameterRenameProperties systemParameterProperties) {
		return new DefaultApplicationManagerImpl(channelConfigProperties, systemParameterProperties);
	}

	@Bean(name="defaultInvokeTimesManager")
	@ConditionalOnMissingBean(InvokeTimesManager.class)
	public InvokeTimesManager invokeTimesManager(@Autowired ApplicationManager applicationManager, @Autowired InvokeApiLogManager invokeApiLogManager) {
		InvokeTimesManager manager = new DefaultInvokeTimesManagerImpl();
		//组装管理器
		manager.setApplicationManager(applicationManager);
		manager.setInvokeApiLogManager(invokeApiLogManager);
		return manager;
	}

	@Bean(name="defaultCheckRepeatManager")
	@ConditionalOnMissingBean(CheckRepeatManager.class)
	public CheckRepeatManager checkRepeatManager(@Autowired EasyApiConfig easyApiConfig, @Autowired CacheManager cacheManager) {
		DefaultCheckRepeatManagerImpl checkRepeatManager= new DefaultCheckRepeatManagerImpl();
		checkRepeatManager.setCacheManager(cacheManager);
		checkRepeatManager.setEasyApiConfig(easyApiConfig);
		return checkRepeatManager;
	}

	@Bean(name="defaultInvokeApiLogManager")
	@ConditionalOnMissingBean(InvokeApiLogManager.class)
	public InvokeApiLogManager invokeApiLogManager() {
		return new DefaultInvokeApiLogManagerImpl();
	}

	@Bean(name="defaultSessionManager")
	@ConditionalOnMissingBean(SessionManager.class)
	public SessionManager sessionManager(@Autowired CacheManager cacheManager, @Autowired EasyApiConfig easyApiConfig) {
		DefaultSessionManagerImpl sessionManager = new DefaultSessionManagerImpl();
		sessionManager.setCacheManager(cacheManager);
		sessionManager.setExpireSeconds(easyApiConfig.getSessionExpireSeconds());
		return sessionManager;
	}
	
	@Bean(name="defaultSignManager")
	@ConditionalOnMissingBean(SignManager.class)
	public SignManager signManager(@Autowired SignEncryptHandler signEncryptHandler) {
		DefaultSignManagerImpl signManager = new DefaultSignManagerImpl();
		return signManager;
	}
	
	//默认国际化处理器
	@Bean(name="defaultLanguageTranslateManager")
	public LanguageTranslateManager defaultLanguageTranslateManager(@Autowired MessageSource messageSource, @Autowired EasyApiConfig easyApiConfig) {
		return new DefaultLanguageTranslateManagerImpl(messageSource, new Locale(easyApiConfig.getLocale()));
	}
	
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
	public SecretManager secretManager(@Autowired ApplicationManager applicationManager) {
		DefaultSecretManagerImpl secretManager = new DefaultSecretManagerImpl(applicationManager);
		return secretManager;
	}
	
	@Bean(name="defaultCacheManager")
	@ConditionalOnMissingBean(CacheManager.class)
	public CacheManager cacheManager(@Autowired LocalReadCacheContainer localReadCacheContainer,
			@Autowired LocalWriteCacheContainer localWriteCacheContainer) {
		DefaultCacheManagerImpl cacheManager = new DefaultCacheManagerImpl();
		cacheManager.setLocalReadCacheContainer(localReadCacheContainer);
		cacheManager.setLocalWriteCacheContainer(localWriteCacheContainer);
		return cacheManager;
	}
	
}
