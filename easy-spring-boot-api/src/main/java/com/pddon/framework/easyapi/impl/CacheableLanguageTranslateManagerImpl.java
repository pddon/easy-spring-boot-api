/**  
* Title CacheableLanguageTranslateManagerImpl.java  
* Description  可缓存的国际化翻译服务
* @author danyuan
* @date Jul 5, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.impl;

import java.text.MessageFormat;

import com.pddon.framework.easyapi.properties.ErrorCodeProperties;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.utils.IOUtils;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.LanguageTranslateManager;

@Service
@Slf4j
@Getter
@Setter
@Primary
public class CacheableLanguageTranslateManagerImpl implements LanguageTranslateManager {

	@Autowired(required = false)
	@Qualifier("languageTranslateManager")
	private LanguageTranslateManager languageTranslateManager;
	
	@Autowired(required = false)
	@Qualifier("defaultLanguageTranslateManager")
	@Lazy
	private DefaultLanguageTranslateManagerImpl defaultLanguageTranslateManager;
	
	@Autowired
	private ErrorCodeProperties errorCodeProperties;
	
	@Autowired
	private EasyApiConfig easyApiConfig;
	
	@Autowired
	private CacheManager cacheManager;
	
	/**
	 * @param key
	 * @param locale
	 * @param params
	 * @return
	 * @author danyuan
	 */
	@Override
	public String get(String key, String locale, Object... params) {
		if(languageTranslateManager == null){
			languageTranslateManager = defaultLanguageTranslateManager;
		}
		
		if(StringUtils.isBlank(key)){
			return key;
		}
		if(StringUtils.isBlank(locale)){
			locale = this.getDefaultLocale();
		}
		String value = key;
		String cacheKey=locale+key;
		boolean exists = true;
		do{
			if(easyApiConfig.getEnableLanguageCache() && cacheManager.exists(cacheKey, easyApiConfig.getLanguageCacheSeconds())){
				//命中本地缓存，直接返回
				value = cacheManager.get(cacheKey, String.class, easyApiConfig.getLanguageCacheSeconds());
				break;
			}
			try{		
				value = languageTranslateManager.get(key, locale);
				if(!StringUtils.isEmpty(value) && !key.equals(value)){
					//存在指定语言的翻译结果
					exists = true;
				}else{
					exists = false;
					if(easyApiConfig.getAlwaysUseDefaultLocale() && !locale.equals(languageTranslateManager.getDefaultLocale())){
						value = languageTranslateManager.get(key);						
					} 
				}	        
			}catch(Exception e){//避免业务字典服务不稳定导致整个翻译器不可用
				if(log.isTraceEnabled()){
					log.trace(e.getMessage());
					log.trace(IOUtils.getThrowableInfo(e));
				}			
			}finally{
				if(!(languageTranslateManager instanceof DefaultLanguageTranslateManagerImpl)){
					//非默认语言翻译器翻译失败，可以使用默认翻译器继续翻译
					if(!exists){
						//当前语言未找到的优先找硬编码的语言信息
			        	String realValue = defaultLanguageTranslateManager.get(key, locale);
			        	if(!StringUtils.isEmpty(realValue)){
			        		value = realValue;
			        	}
					}
					if(easyApiConfig.getAlwaysUseDefaultLocale()){
						if(StringUtils.isEmpty(value) || key.equals(value)){
				        	//未查询到该字典,则使用默认字典服务读取系统预置字典信息
				        	value = defaultLanguageTranslateManager.get(key, defaultLanguageTranslateManager.getDefaultLocale());
				        }
					}										
				}
				if(easyApiConfig.getAlwaysUseDefaultLocale()){
					//添加业务异常默认语言国际化内容
					if(StringUtils.isEmpty(value) || key.equals(value) && (errorCodeProperties.getBusinessErrorCodes() != null)){
						if(errorCodeProperties.getBusinessErrorCodes().containsKey(key)){
							value = errorCodeProperties.getBusinessErrorCodes().get(key).getDesc();
						}						        	
			        }	
				}				
				if(StringUtils.isEmpty(value)){//如果为空也进行缓存，防止缓存穿透
					value = "";		        
				}
				if(easyApiConfig.getEnableLanguageCache()){
					cacheManager.set(cacheKey, value, easyApiConfig.getLanguageCacheSeconds());	
				}		        			
			}        
		} while(false);
		
		//统一做参数格式化
        if(!StringUtils.isEmpty(value) && (params != null) && (params.length > 0)){
        	try{
        		return MessageFormat.format(value, params);
        	}catch(Exception e){
        		//格式化错误直接返回非参数化的多语言信息即可
        		if(log.isTraceEnabled()){
        			log.trace("国际化字典格式化错误:{}",e.getMessage());
        		}
        	}
        }
		return value;
	}

	/**
	 * @param key
	 * @param params
	 * @return
	 * @author danyuan
	 */
	@Override
	public String get(String key, Object... params) {
		return this.get(key, languageTranslateManager.getDefaultLocale(), params);
	}

	/**
	 * @param locale
	 * @author danyuan
	 */
	@Override
	public void setDefaultLocale(String locale) {
		languageTranslateManager.setDefaultLocale(locale);
	}

	/**
	 * @author danyuan
	 */
	@Override
	public String getDefaultLocale() {
		return languageTranslateManager.getDefaultLocale();
	}

}
