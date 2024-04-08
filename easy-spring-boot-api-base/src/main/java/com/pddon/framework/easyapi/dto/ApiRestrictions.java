/**  
* Title ApiRestrictions.java  
* Description  api限制说明
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import java.io.Serializable;

import com.pddon.framework.easyapi.encrypt.DataEncryptHandler;

import lombok.Data;

@Data
public class ApiRestrictions implements Serializable{
	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 接口会话内访问开关
	 */
	private Boolean session = false;
	/**
	 * 接口请求验签开关
	 */
	private Boolean requestSign = false;
	/**
	 * 接口响应验签开关
	 */
	private Boolean responseSign = false;
	
	/**
	 * 防重复提交开关
	 */
	private Boolean uniqueSubmit = false;
	/**
	 * 打印接口调用日志开关
	 */
	private Boolean apiLog = true;
	/**
	 * 接口调用次数控制开关
	 */
	private Boolean invokeTimesControl = true;
	/**
	 * 是否需要忽略响应内容，不进行包装
	 */
	private Boolean ignoreResponseWrapper = false;
	/**
	 * 响应是否需要加密
	 */
	private Boolean encrptResponseData = false;
	
	private Class<? extends DataEncryptHandler> encrptHandlerClass = null;
}
