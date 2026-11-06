/**  
* Title MethodCacheManager.java  
* Description  
* @author danyuan
* @date Dec 19, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.cache;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pddon.framework.easyapi.consts.CacheKeyMode;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.MethodInvokeUtil;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pddon.framework.easyapi.CacheManager;

@Service
@Slf4j
public class MethodCacheManager {

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private SystemParameterRenameProperties systemParameterRenameProperties;

	public String getFieldValue(String keyId, String[] parameters, Object [] args, Method method){
		//提取参数信息
		Map<String, String> nameValueMap = new HashMap<>();
		int i = 0;
		String paramName = "";
		for(Object param : args){
			paramName = MethodInvokeUtil.getBaseTypeParamName(method.getParameterAnnotations()[i]);
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
		return nameValueMap.get(keyId);
	}
	/**
	 * 生成缓存key（旧格式，与改造前完全一致）
	 */
	public String getCacheKey(String prefix, CacheKeyMode mode, String keyId, String[] parameters, Object [] args, Class<?> targetClass, Method method){
		return getCacheKey(prefix, mode, keyId, parameters, args, targetClass, method, false);
	}

	/**
	 * 生成缓存key
	 * @param encodeParamKey 自动参数key是否使用新格式（对应easyapi.method-cache.auto-key-v2）。
	 *                       默认false保持旧格式；true时参数值做URL编码、显式区分null参数，
	 *                       避免"-"拼接歧义，并按字段名精确剔除系统参数。
	 */
	public String getCacheKey(String prefix, CacheKeyMode mode, String keyId, String[] parameters, Object [] args, Class<?> targetClass, Method method, boolean encodeParamKey){
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
		if(CacheKeyMode.AUTO_BY_PARAMS.equals(mode) && encodeParamKey){
			return buildStrictAutoKey(prefix, parameters, args, method);
		}
		//提取参数信息
		Map<String, String> nameValueMap = new HashMap<>();
		int i = 0;
		String paramName = "";
		for(Object param : args){
			paramName = MethodInvokeUtil.getBaseTypeParamName(method.getParameterAnnotations()[i]);
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
					.filter(key -> !isSystemParamKey(key))
					.map(key -> nameValueMap.get(key))
					.collect(Collectors.joining("-"));
			StringBuffer key = new StringBuffer(prefix).append(":").append(paramKey);
			return key.toString();
		}
		return prefix;
	}

	/**
	 * 新格式自动参数key。
	 *
	 * 与旧格式的差异：
	 * 1. 参数值URL编码后以"&amp;"连接，不再用"-"直接拼接，避免("x-y","z")与("x","y-z")碰撞；
	 * 2. null参数使用显式占位符，且保留参数位置（参数名参与key），避免(null,"x")与("x",null)碰撞；
	 * 3. 以"v2"标记区分旧格式key，保证新旧key不会互相命中。
	 * 4. 字段名按完整字段名精确剔除系统参数（且仅剔除最后一段字段名），避免design/signal被误剔除。
	 */
	private String buildStrictAutoKey(String prefix, String[] parameters, Object [] args, Method method){
		StringBuffer key = new StringBuffer(prefix).append(":v2:");
		if(args == null || args.length == 0){
			return key.toString();
		}
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		List<String> segments = new ArrayList<>();
		for(int i = 0; i < args.length; i++){
			String paramName = MethodInvokeUtil.getBaseTypeParamName(parameterAnnotations[i]);
			if(StringUtils.isBlank(paramName)){
				if(parameters != null && i < parameters.length){
					paramName = parameters[i];
				}else{
					paramName = "p" + i;
				}
			}
			Object arg = args[i];
			if(arg == null){
				//显式占位，区分"参数为null"与"参数被省略"
				segments.add(encode(paramName) + "=" + NULL_VALUE_MARKER);
				continue;
			}
			Map<String, String> nameValueMap = BeanPropertyUtil.objToStringMap(arg, paramName);
			List<String> fieldKeys = new ArrayList<>(nameValueMap.keySet());
			//排序保证同一参数集合生成的key稳定，不受Map迭代顺序影响
			Collections.sort(fieldKeys);
			for(String fieldKey : fieldKeys){
				if(isSystemParamKey(fieldKey)){
					continue;
				}
				segments.add(encode(fieldKey) + "=" + encode(nameValueMap.get(fieldKey)));
			}
		}
		return key.append(String.join("&", segments)).toString();
	}

	/**
	 * null参数占位符。取含"%"的形式，URL编码后的真实值不会以"%"开头（"%"会被编码为"%25"），因此不会与真实值碰撞
	 */
	private static final String NULL_VALUE_MARKER = "%00NULL";

	private String encode(String value){
		try{
			return URLEncoder.encode(value == null ? "" : value, "UTF-8");
		}catch(UnsupportedEncodingException e){
			throw new IllegalStateException("UTF-8 encoding is not supported", e);
		}
	}

	/**
	 * 是否为需要从自动参数key中剔除的系统参数。
	 * 旧实现用key.contains(...)，会把"design"/"signal"等业务字段误剔除；
	 * 这里改为取key的最后一段字段名（去掉数组下标）做精确匹配。
	 */
	private boolean isSystemParamKey(String key){
		if(StringUtils.isBlank(key) || systemParameterRenameProperties == null){
			return false;
		}
		String fieldName = key;
		int dotIndex = key.lastIndexOf('.');
		if(dotIndex >= 0){
			fieldName = key.substring(dotIndex + 1);
		}
		int bracketIndex = fieldName.indexOf('[');
		if(bracketIndex >= 0){
			fieldName = fieldName.substring(0, bracketIndex);
		}
		return equalsSystemParamName(fieldName, systemParameterRenameProperties.getTimestamp())
				|| equalsSystemParamName(fieldName, systemParameterRenameProperties.getSign())
				|| equalsSystemParamName(fieldName, systemParameterRenameProperties.getClientId())
				|| equalsSystemParamName(fieldName, systemParameterRenameProperties.getLocale())
				|| equalsSystemParamName(fieldName, systemParameterRenameProperties.getAppId())
				|| equalsSystemParamName(fieldName, systemParameterRenameProperties.getSessionId())
				|| equalsSystemParamName(fieldName, systemParameterRenameProperties.getClientIp())
				|| equalsSystemParamName(fieldName, systemParameterRenameProperties.getTimeZone())
				|| equalsSystemParamName(fieldName, systemParameterRenameProperties.getUserId())
				|| equalsSystemParamName(fieldName, systemParameterRenameProperties.getCountryCode())
				|| equalsSystemParamName(fieldName, systemParameterRenameProperties.getVersionCode());
	}

	private boolean equalsSystemParamName(String fieldName, String systemParamName){
		return systemParamName != null && !systemParamName.isEmpty() && systemParamName.equals(fieldName);
	}
	
	public Object getResult(String key, Class<?> classType, CacheManager cacheManager, Long expireSeconds, CacheExpireMode expireMode){
		if(cacheManager == null){
			cacheManager = this.cacheManager;
		}
		return cacheManager.get(key, classType, expireSeconds, expireMode);
	}
	
	public void cacheResult(String key ,Object result, CacheManager cacheManager, Long expireSeconds, CacheExpireMode expireMode){
		if(cacheManager == null){
			cacheManager = this.cacheManager;
		}
		cacheManager.set(key, result, expireSeconds, expireSeconds, expireMode);
	}
	
	public void remove(String key , CacheManager cacheManager, Long expireSeconds, CacheExpireMode expireMode){
		if(cacheManager == null){
			cacheManager = this.cacheManager;
		}
		cacheManager.remove(key, expireSeconds, expireMode);
	}
	
}
