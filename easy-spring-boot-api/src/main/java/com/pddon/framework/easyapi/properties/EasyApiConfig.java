/**  
* Title EasyApiConfig.java
* Description  
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.properties;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import com.pddon.framework.easyapi.utils.ClassOriginCheckUtil;

import lombok.Data;
import lombok.experimental.Accessors;
@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "easyapi")
public class EasyApiConfig implements Serializable {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 是否启用EasyAPi框架
	 */
	private Boolean enable;
	/**
	 * 是否开启swagger
	 */
	private Boolean enableSwagger;
	/**
	 * 默认语言
	 */
	private String locale;
	/**
	 * 是否开启多语言翻译缓存
	 */
	private Boolean enableLanguageCache;
	/**
	 * 多语言缓存的最长时间，单位秒
	 */
	private Long languageCacheSeconds;
	/**
	 * 当未找到该语言的翻译时，是否使用默认翻译内容
	 */
	private Boolean alwaysUseDefaultLocale;
	/**
	 * 防重复提交码过期时间
	 */
	private Long noSubmitRepeatTimeoutSeconds;
	/**
	 * 会话失效时间
	 */
	private Long sessionExpireSeconds;
	/**
	 * 是否强制对所有api响应添加响应壳
	 */
	private Boolean forceAutoAddResponseWrapper;
	/**
	 * 根包名集合,以逗号分隔
	 */
	private String basePackages;
	/**
	 * 是否在控制台打印所有配置信息
	 */
	private Boolean printAllProperties;
	/**
	 * 禁用方法缓存
	 */
	private Boolean disableMethodCache;
	public EasyApiConfig(){
		this.enable = true;
		this.enableSwagger = true;
		this.locale = Locale.CHINA.toString();
		this.enableLanguageCache = true;
		this.languageCacheSeconds = 180L;//默认缓存3分钟
		this.noSubmitRepeatTimeoutSeconds = 120L;//默认2分钟
		this.sessionExpireSeconds = 600L; //默认10分钟
		this.alwaysUseDefaultLocale = true;
		this.forceAutoAddResponseWrapper = true;
		this.basePackages = "";
		this.printAllProperties = false;
		this.disableMethodCache = false;
	}
	
	public Set<String> getAllBasePackages(){
		Set<String> set = new HashSet<>();
		if(!StringUtils.isEmpty(this.basePackages)){
			String[] pkgs = this.basePackages.split(",");
			for(String pkg : pkgs){
				set.add(pkg);
			}
		}
		//添加启动类所在的包
		set.add(ClassOriginCheckUtil.getDeafultBasePackage());
		return set;
	}
	
}
