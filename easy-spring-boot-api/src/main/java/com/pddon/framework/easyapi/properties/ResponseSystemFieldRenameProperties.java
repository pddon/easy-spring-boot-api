/**  
* Title ResponseWrapper.java  
* Description  
* @author danyuan
* @date Oct 19, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.properties;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "easyapi.api.response.field.rename")
public class ResponseSystemFieldRenameProperties implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	public static final String CODE = "code";
	public static final String SUB_CODE = "subCode";
	public static final String MSG = "msg";
	public static final String DATA = "data";
	public static final String SIGN = "sign";
	public static final String TIMESTAMP = "timestamp";
	
	/**
	 * 响应错误码字段名
	 */
	private String code = CODE;
	/**
	 * 响应错误子码，详细业务错误标识
	 */
	private String subCode = SUB_CODE;
	/**
	 * 响应描述信息字段名
	 */
	private String msg = MSG;
	/**
	 * 响应内容字段名
	 */
	private String data = DATA;
	/**
	 * 响应数字签名字段名
	 */
	private String sign	= SIGN;
	/**
	 * 响应时间戳字段名
	 */
	private String timestamp = TIMESTAMP;
}
