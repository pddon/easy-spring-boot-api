/**  
* Title DefaultConfigPropertiesValue.java  
* Description  系统默认配置参数值
* @author danyuan
* @date Nov 30, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.consts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import lombok.Getter;

@Getter
public enum DefaultConfigPropertiesValue {
	SRPRING_MSG_BASENAME("spring.messages.basename","META-INF/i18n/easyApiMessages,i18n/messages/messages", DefaultConfigValueMode.PREPEND)
	,EASYAPI_BASE_PACKAGES("easyapi.basePackages","com.pddon.framework.easyapi", DefaultConfigValueMode.PREPEND)
	,EASYAPI_DEFAULT_LOG_LEVEL("logging.level.com.pddon.framework.easyapi","INFO", DefaultConfigValueMode.OPTION)
	,EASYAPI_ENABLE("easyapi.enable",true, DefaultConfigValueMode.OPTION)
	,EASYAPI_ENABLE_SWAGGER("easyapi.enableSwagger",true, DefaultConfigValueMode.OPTION)
	,SRPRING_MSG_USE_CODE_AS_DEFAULT_MESSAGE("spring.messages.useCodeAsDefaultMessage",false, DefaultConfigValueMode.FORCE_DEFAULT)
	,SRPRING_MSG_FALLBACK_TO_SYSTEM_LOCALE("spring.messages.fallbackToSystemLocale",false, DefaultConfigValueMode.FORCE_DEFAULT)
	,KNIFE4J_BASIC_ENABLE("knife4j.basic.enable",true, DefaultConfigValueMode.OPTION)
	,KNIFE4J_ENABLE("knife4j.enable",true, DefaultConfigValueMode.OPTION)
	,KNIFE4J_BASIC_USERNAME("knife4j.basic.username","develop", DefaultConfigValueMode.OPTION)
	,KNIFE4J_BASIC_PASSWORD("knife4j.basic.password","pddon", DefaultConfigValueMode.OPTION)
	,KNIFE4J_DOCUMENTS_0_GROUP("knife4j.documents[0].group","使用说明文档", DefaultConfigValueMode.OPTION)
	,KNIFE4J_DOCUMENTS_0_NAME("knife4j.documents[0].name","全局通用接口调用说明", DefaultConfigValueMode.OPTION)
	,KNIFE4J_DOCUMENTS_0_LOCATIONS("knife4j.documents[0].locations","classpath*:markdown/*", DefaultConfigValueMode.OPTION)
	;
	private String key;
	
	private Object defaultValue;
	/**
	 * 默认值使用模式
	 */
	private DefaultConfigValueMode mode;

	
	private DefaultConfigPropertiesValue(String key, Object defaultValue, DefaultConfigValueMode mode){
		this.key = key;
		this.defaultValue = defaultValue;
		this.mode = mode;
	}
	
	
	private DefaultConfigPropertiesValue(String key, Object defaultValue){
		this.key = key;
		this.defaultValue = defaultValue;
		this.mode = DefaultConfigValueMode.OPTION; //默认值，可被配置覆盖
	}
	
	
	public Object getComposeValue(Object originalValue){
		if(DefaultConfigValueMode.FORCE_DEFAULT.equals(this.mode) || StringUtils.isEmpty(originalValue)){
			return this.defaultValue;
		}
		if(DefaultConfigValueMode.OPTION.equals(this.mode) || this.defaultValue.equals(originalValue)){
			return originalValue;
		}		
		if(originalValue instanceof String){
			StringBuffer buffer = new StringBuffer();
			Set<String> set = new HashSet<>();
			for(String val : ((String) originalValue).split(",")){
				if(!set.contains(val)){
					set.add(val);
				}
			}
			String[] defaultValues = ((String) this.defaultValue).split(",");
			if(DefaultConfigValueMode.APPEND.equals(this.mode)){
				buffer.append(originalValue);
			}
			for(String value : defaultValues){
				if(!set.contains(value)){
					if(DefaultConfigValueMode.APPEND.equals(this.mode)){
						buffer.append(",").append(value);
					}else{
						buffer.append(value).append(",");
					}
				}
			}
			if(DefaultConfigValueMode.PREPEND.equals(this.mode)){
				buffer.append(originalValue);
			}			
			return buffer.toString();
		}
		return originalValue;
	}
	
	public static Map<String,Object> getAllDefaultConfigs(){
		Map<String,Object> map = new HashMap<>();
		for(DefaultConfigPropertiesValue config : DefaultConfigPropertiesValue.values()){
			map.put(config.getKey(), config.getDefaultValue());
		}
		return map;
	}
}
