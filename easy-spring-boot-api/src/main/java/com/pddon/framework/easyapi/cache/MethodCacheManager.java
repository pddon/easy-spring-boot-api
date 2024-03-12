/**  
* Title MethodCacheManager.java  
* Description  
* @author danyuan
* @date Dec 19, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.cache;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.pddon.framework.easyapi.consts.CacheKeyMode;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.MethdInvokeUtil;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pddon.framework.easyapi.CacheManager;

@Service
@Slf4j
public class MethodCacheManager {

	@Autowired
	private SystemParameterRenameProperties systemParameterRenameProperties;
	
	public String getCacheKey(String prefix, CacheKeyMode mode, String keyId, String[] parameters, Object [] args, Class<?> targetClass, Method method){
		//先提取缓存前缀
		if(StringUtils.isBlank(prefix)){
			//自动生成缓存前缀
			StringBuffer buffer = new StringBuffer(targetClass.getSimpleName());
			buffer.append(":")
			.append(method.getName());
			prefix = buffer.toString();
		}
		if(CacheKeyMode.AUTO_BY_METHOD.equals(mode)){
			//方法级缓存，直接返回前缀即可
			return prefix;
		}
		//提取参数信息
		Map<String, String> nameValueMap = new HashMap<>();
		int i = 0;
		String paramName = "";
		for(Object param : args){
			paramName = MethdInvokeUtil.getBaseTypeParamName(method.getParameterAnnotations()[i]);
			if(StringUtils.isBlank(paramName)){
				if(parameters != null){
					paramName = parameters[i];
				}else{
					paramName = "p"+i;
				}
			}
			nameValueMap.putAll(BeanPropertyUtil.objToStringMap(param, paramName));
			i++;
		}
		if(CacheKeyMode.CUSTOM_ID.equals(mode)){
			String id = nameValueMap.get(keyId);
			if(StringUtils.isBlank(id)){
				log.warn("缓存参数配置错误，未找到参数信息:[{}]", keyId);
				return null;
				//throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam("参数化缓存配置错误:"+keyId);
			}
			StringBuffer key = new StringBuffer(prefix).append(":").append(id);
			return key.toString();
		}else if(CacheKeyMode.AUTO_BY_PARAMS.equals(mode)){
			String paramKey = nameValueMap.keySet().stream()
					.filter(key -> !key.contains(systemParameterRenameProperties.getTimestamp())
							&& !key.contains(systemParameterRenameProperties.getSign())
							&& !key.contains(systemParameterRenameProperties.getClientId())
							&& !key.contains(systemParameterRenameProperties.getLocale())
							&& !key.contains(systemParameterRenameProperties.getAppId())
							&& !key.contains(systemParameterRenameProperties.getSessionId())
							&& !key.contains(systemParameterRenameProperties.getClientIp())
							&& !key.contains(systemParameterRenameProperties.getTimeZone())
							&& !key.contains(systemParameterRenameProperties.getUserId())
							&& !key.contains(systemParameterRenameProperties.getCountryCode())
							&& !key.contains(systemParameterRenameProperties.getVersionCode()))
					.map(key -> nameValueMap.get(key))
					.collect(Collectors.joining("-"));
			StringBuffer key = new StringBuffer(prefix).append(":").append(paramKey);
			return key.toString();
		}
		return prefix;
	}
	
	public Object getResult(String key, CacheManager cacheManager, Long expireSeconds, CacheExpireMode expireMode){
		
		return cacheManager.get(key, expireSeconds, expireMode);
	}
	
	public void cacheResult(String key ,Object result, CacheManager cacheManager, Long expireSeconds, CacheExpireMode expireMode){
		
		cacheManager.set(key, result, expireSeconds, expireSeconds, expireMode);
	}
	
	public void remove(String key , CacheManager cacheManager, Long expireSeconds, CacheExpireMode expireMode){
		
		cacheManager.remove(key, expireSeconds, expireMode);
	}
	
}
