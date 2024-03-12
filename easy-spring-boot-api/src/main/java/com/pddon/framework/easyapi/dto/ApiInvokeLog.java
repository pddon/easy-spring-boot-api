/**  
* Title ApiInvokeLog.java  
* Description  
* @author danyuan
* @date Oct 31, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiInvokeLog implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	/**
	 * 接口名
	 */
	private String apiName;
	/**
	 * 接口URI
	 */
	private String apiUri;
	/**
	 * http请求类型
	 */
	private String apiMethod;
	/**
	 * 接口版本
	 */
	private String apiVersion;	
	/**
	 * 应用ID，可以按应用维度控制api的访问权限
	 */
	private String appId;
	/**
	 * 渠道ID，如：安卓、iOS、微信、官网等等，可以按访问渠道控制api的访问权限
	 */
	private String channelId;
	
	/**
	 * 会话ID，用于便于用户认证后在一定时间段内进行有状态的访问后续接口
	 */
	private String sessionId;
	/**
	 * 用户访问唯一标识
	 */
	private String userId;
	/**
	 * 客户端ID,用于区分是哪个APP或者其他客户端的不同分支
	 */
	private String clientId;
	/**
	 * 客户端版本号
	 */
	private String versionCode;
	/**
	 * 时间戳
	 */
	private Long timestamp;
	/**
	 * 客户端IP地址
	 */
	private String clientIp;
	/**
	 * 系统参数
	 */
	private Map<String, Object> systemParams;
	/**
	 * 请求入参
	 */
	private Object[] requestParams;
	/**
	 * 响应参数
	 */
	private Object response;	
}
