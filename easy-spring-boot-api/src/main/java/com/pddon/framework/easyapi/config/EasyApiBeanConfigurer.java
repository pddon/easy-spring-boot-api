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
import com.pddon.framework.easyapi.state.DistributedStateStore;
import com.pddon.framework.easyapi.state.FallbackStateStore;
import com.pddon.framework.easyapi.state.LocalStateStore;
import com.pddon.framework.easyapi.state.RedisStateStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

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
import com.pddon.framework.easyapi.cache.RedisStateCacheManager;
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

	@Bean(name="applicationManager")
	@ConditionalOnMissingBean(ApplicationManager.class)
	public ApplicationManager applicationManager(@Autowired ChannelConfigProperties channelConfigProperties, @Autowired SystemParameterRenameProperties systemParameterProperties) {
		return new DefaultApplicationManagerImpl(channelConfigProperties, systemParameterProperties);
	}

	@Bean(name="invokeTimesManager")
	@ConditionalOnMissingBean(InvokeTimesManager.class)
	public InvokeTimesManager invokeTimesManager(@Autowired ApplicationManager applicationManager, @Autowired InvokeApiLogManager invokeApiLogManager) {
		InvokeTimesManager manager = new DefaultInvokeTimesManagerImpl();
		//组装管理器
		manager.setApplicationManager(applicationManager);
		manager.setInvokeApiLogManager(invokeApiLogManager);
		return manager;
	}

	@Bean(name="checkRepeatManager")
	@ConditionalOnMissingBean(CheckRepeatManager.class)
	public CheckRepeatManager checkRepeatManager(@Autowired EasyApiConfig easyApiConfig, @Autowired CacheManager cacheManager,
			@Autowired DistributedStateStore distributedStateStore) {
		DefaultCheckRepeatManagerImpl checkRepeatManager= new DefaultCheckRepeatManagerImpl();
		checkRepeatManager.setCacheManager(cacheManager);
		checkRepeatManager.setEasyApiConfig(easyApiConfig);
		checkRepeatManager.setStateStore(distributedStateStore);
		return checkRepeatManager;
	}

	@Bean(name="invokeApiLogManager")
	@ConditionalOnMissingBean(InvokeApiLogManager.class)
	public InvokeApiLogManager invokeApiLogManager() {
		return new DefaultInvokeApiLogManagerImpl();
	}

	@Bean(name="sessionManager")
	@ConditionalOnMissingBean(SessionManager.class)
	public SessionManager sessionManager(@Autowired CacheManager cacheManager, @Autowired EasyApiConfig easyApiConfig) {
		DefaultSessionManagerImpl sessionManager = new DefaultSessionManagerImpl();
		sessionManager.setCacheManager(cacheManager);
		sessionManager.setExpireSeconds(easyApiConfig.getSessionExpireSeconds());
		return sessionManager;
	}
	
	@Bean(name="signManager")
	@ConditionalOnMissingBean(SignManager.class)
	public SignManager signManager(@Autowired SignEncryptHandler signEncryptHandler) {
		DefaultSignManagerImpl signManager = new DefaultSignManagerImpl();
		return signManager;
	}
	
	//默认国际化处理器
	@Bean(name="languageTranslateManager")
	public LanguageTranslateManager languageTranslateManager(@Autowired MessageSource messageSource, @Autowired EasyApiConfig easyApiConfig) {
		return new DefaultLanguageTranslateManagerImpl(messageSource, new Locale(easyApiConfig.getLocale()));
	}
	
	@Bean(name="signEncryptHandler")
	@ConditionalOnMissingBean(SignEncryptHandler.class)
	public SignEncryptHandler signEncryptHandler() {
		return new Sha1SignEncryptHandler();
	}
	
	@Bean(name="dataEncryptHandler")
	@ConditionalOnMissingBean(AESDataEncryptHandler.class)
	public DataEncryptHandler dataEncryptHandler(@Autowired SecretManager secretManager) {
		return new AESDataEncryptHandler().setSecretManager(secretManager);
	}
	
	@Bean(name="secretManager")
	@ConditionalOnMissingBean(SecretManager.class)
	public SecretManager secretManager(@Autowired ApplicationManager applicationManager) {
		DefaultSecretManagerImpl secretManager = new DefaultSecretManagerImpl(applicationManager);
		return secretManager;
	}
	
	@Bean(name="cacheManager")
	@ConditionalOnMissingBean(CacheManager.class)
	public CacheManager cacheManager(@Autowired LocalReadCacheContainer localReadCacheContainer,
			@Autowired LocalWriteCacheContainer localWriteCacheContainer,
			@Autowired ObjectProvider<DistributedStateStore> stateStoreProvider) {
		//state-store解析为redis时，会话、方法缓存、国际化缓存、验证码等统一走分布式存储；
		//local（未引入Redis依赖、type=local、auto且无Redis）时保持原有本地缓存行为
		DistributedStateStore stateStore = stateStoreProvider.getIfAvailable();
		if(stateStore != null && "redis".equalsIgnoreCase(stateStore.getType())){
			return new RedisStateCacheManager(stateStore);
		}
		DefaultCacheManagerImpl cacheManager = new DefaultCacheManagerImpl();
		cacheManager.setLocalReadCacheContainer(localReadCacheContainer);
		cacheManager.setLocalWriteCacheContainer(localWriteCacheContainer);
		return cacheManager;
	}

	/**
	 * 状态存储key前缀：优先取easyapi.state-store.key-prefix，为空时使用"easyapi:应用名:"，避免多个应用共享Redis时冲突
	 */
	static String resolveKeyPrefix(EasyApiConfig easyApiConfig, Environment environment){
		String prefix = easyApiConfig.getStateStore().getKeyPrefix();
		if(prefix == null || prefix.trim().isEmpty()){
			String appName = environment == null ? null : environment.getProperty("spring.application.name");
			if(appName == null || appName.trim().isEmpty()){
				appName = "default";
			}
			prefix = "easyapi:" + appName.trim() + ":";
		}
		return prefix;
	}

	/**
	 * 本地状态存储：未引入/未启用Redis时的默认实现，行为与框架原有本地缓存一致
	 */
	@Bean(name="localStateStore")
	public DistributedStateStore localStateStore(@Autowired EasyApiConfig easyApiConfig,
			@Autowired LocalWriteCacheContainer localWriteCacheContainer,
			@Autowired Environment environment,
			@Autowired ApplicationContext applicationContext) {
		String type = easyApiConfig.getStateStore().getType();
		if("redis".equalsIgnoreCase(type) && !applicationContext.containsBean("redisStateStore")){
			throw new IllegalStateException("easyapi.state-store.type=redis，但当前应用未引入spring-boot-starter-data-redis或未创建StringRedisTemplate");
		}
		return new LocalStateStore(localWriteCacheContainer, resolveKeyPrefix(easyApiConfig, environment));
	}

	/**
	 * Redis状态存储：仅当classpath中存在StringRedisTemplate时注册；
	 * 存在Redis时优先注入（@Primary），未引入Redis依赖时本配置整体不生效，回退到localStateStore。
	 */
	@Configuration
	@ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
	@ConditionalOnProperty(prefix = "easyapi.state-store", name = "redis-enabled", havingValue = "true", matchIfMissing = true)
	protected static class RedisStateStoreConfiguration {

		@Bean(name="redisStateStore")
		@Primary
		public DistributedStateStore redisStateStore(@Autowired EasyApiConfig easyApiConfig,
				@Autowired ObjectProvider<StringRedisTemplate> redisTemplateProvider,
				@Autowired LocalWriteCacheContainer localWriteCacheContainer,
				@Autowired Environment environment) {
			String type = easyApiConfig.getStateStore().getType();
			String keyPrefix = resolveKeyPrefix(easyApiConfig, environment);
			LocalStateStore localStore = new LocalStateStore(localWriteCacheContainer, keyPrefix);
			if("local".equalsIgnoreCase(type)){
				return localStore;
			}
			StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
			if(redisTemplate == null){
				if("redis".equalsIgnoreCase(type)){
					throw new IllegalStateException("easyapi.state-store.type=redis，但未找到StringRedisTemplate Bean");
				}
				return localStore;
			}
			RedisStateStore redisStore = new RedisStateStore(redisTemplate, keyPrefix);
			if(easyApiConfig.getStateStore().isFallbackToLocalOnFailure()){
				return new FallbackStateStore(redisStore, localStore,
						easyApiConfig.getStateStore().getFallbackWarnIntervalSeconds());
			}
			return redisStore;
		}
	}
}
