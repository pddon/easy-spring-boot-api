/**  
* Title SystemParameterProperties.java  
* Description  
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.properties;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "easyapi.api.request.parameter.rename")
public class SystemParameterRenameProperties implements InitializingBean{
	public static final Map<String, String> DEFAULT_PARAM_MAP = new HashMap<>();
	
	public static final String SECRET_ID = "secretId";
	public static final String APP_ID = "appId";
	public static final String CHANNEL_ID = "channelId";
	public static final String SESSION_ID = "sessionId";
	public static final String USER_ID = "userId";
	public static final String SIGN = "sign";
	public static final String VERSION_CODE = "versionCode";
	public static final String REPEAT_CODE = "repeatCode";
	public static final String TIMESTAMP = "timestamp";
	public static final String LOCALE = "locale";
	public static final String CURRENCY = "currency";
	public static final String COUNTRY_CODE = "countryCode";
	public static final String TIME_ZONE = "timeZone";
	public static final String CLIENT_ID = "clientId";
	public static final String CLIENT_IP = "clientIp";
	
	/**
	 * 秘钥ID，可以按秘钥维度控制api的访问权限
	 */
	private String secretId = SECRET_ID;
	/**
	 * 应用ID，可以按应用维度控制api的访问权限
	 */
	private String appId = APP_ID;
	/**
	 * 渠道ID，如：安卓、iOS、微信、官网等等，可以按访问渠道控制api的访问权限
	 */
	private String channelId = CHANNEL_ID;
	
	/**
	 * 会话ID，用于便于用户认证后在一定时间段内进行有状态的访问后续接口
	 */
	private String sessionId = SESSION_ID;
	/**
	 * 用户访问唯一标识
	 */
	private String userId = USER_ID;
	/**
	 * 客户端通过秘钥+参数进行验签后生成的数字签名
	 */
	private String sign = SIGN;
	/**
	 * 客户端版本号
	 */
	private String versionCode = VERSION_CODE;
	/**
	 * 防重复提交码,用来控制接口防重复提交
	 */
	private String repeatCode = REPEAT_CODE;
	/**
	 * 时间戳
	 */
	private String timestamp = TIMESTAMP;
	/**
	 * 客户端类型标识
	 */
	private String clientId = CLIENT_ID;
	/**
	 * 客户端调用者IP
	 */
	private String clientIp = CLIENT_IP;
	
	/****************国际化相关的系统参数****************/	
	/**
	 * 语言，如： en_US、zh_CN等等
	 */
	private String locale = LOCALE;
	/**
	 * 货币类型,如：USD、CNY等等
	 */
	private String currency = CURRENCY;
	/**
	 * 国家码，如： US、CN等等
	 */
	private String countryCode = COUNTRY_CODE;
	/**
	 * 时区,如： GTM+8   北京时间所在时区
	 */
	private String timeZone = TIME_ZONE;
	
	/**
	 * @author danyuan
	 */
	@Override
	public void afterPropertiesSet() throws Exception {//允许自定义系统参数名
		DEFAULT_PARAM_MAP.put(LOCALE, locale);
		DEFAULT_PARAM_MAP.put(APP_ID, appId);
		DEFAULT_PARAM_MAP.put(CURRENCY, currency);
		DEFAULT_PARAM_MAP.put(COUNTRY_CODE, countryCode);
		DEFAULT_PARAM_MAP.put(TIME_ZONE, timeZone);
		DEFAULT_PARAM_MAP.put(SESSION_ID, sessionId);
		DEFAULT_PARAM_MAP.put(USER_ID, userId);
		DEFAULT_PARAM_MAP.put(SIGN, sign);
		DEFAULT_PARAM_MAP.put(CHANNEL_ID, channelId);
		DEFAULT_PARAM_MAP.put(VERSION_CODE, versionCode);
		DEFAULT_PARAM_MAP.put(REPEAT_CODE, repeatCode);
		DEFAULT_PARAM_MAP.put(TIMESTAMP, timestamp);
		DEFAULT_PARAM_MAP.put(CLIENT_ID, clientId);
		DEFAULT_PARAM_MAP.put(CLIENT_IP, clientIp);		
	}

	public static String getSysParamName(String key){
		return DEFAULT_PARAM_MAP.get(key);
	}

	public static void setSysParamName(String key, String newName){
		DEFAULT_PARAM_MAP.put(key, newName);
	}
	
}
