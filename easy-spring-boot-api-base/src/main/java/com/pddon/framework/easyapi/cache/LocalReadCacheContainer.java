/**  
* Title LocalReadCacheContainer.java  
* Description  本地缓存管理器，以失效时间为key进行管理
* @author danyuan
* @date Dec 17, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.cache;

import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.properties.LocalCacheConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocalReadCacheContainer extends LocalCacheContainer{

	/**
	 * @author danyuan
	 */
	public LocalReadCacheContainer() {
		super(CacheExpireMode.EXPIRE_AFTER_REDA);
	}	

	/**
	 * 使用配置项easyapi.local-cache.lock-free控制是否关闭全局锁
	 */
	@Autowired
	public LocalReadCacheContainer(ObjectProvider<LocalCacheConfig> localCacheConfigProvider) {
		super(CacheExpireMode.EXPIRE_AFTER_REDA, resolveLockFree(localCacheConfigProvider),
				resolveMaxCacheSize(localCacheConfigProvider), resolveMaxCacheNum(localCacheConfigProvider));
	}
	
}
