/**  
* Title BaseDeviceRequest.java  
* Description  
* @author danyuan
* @date Nov 6, 2018
* @version 1.0.0
* blog pddon.cn
*/ 
package com.pddon.framework.easyapi.controller.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *@author danyuan
 *@date Nov 6, 2018
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
@Accessors(chain=true)
@ApiModel(value="BaseMntRequest",description="管理后台请求系统参数,系统参数全部忽略签名")
public class BaseMntRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(
			value="当前登录用户Id",
			example="10032984"
			)
	private String userId;	
	
	@ApiModelProperty(
			value="当前登录用户所属的国家码",
			example="US"
			)
	private String countryCode;
	
	@ApiModelProperty(
			required=false,
			allowEmptyValue=false,
			value="语言",
			example="en_US"
			)
	private String locale;
	
	@ApiModelProperty(
			required=false,
			allowEmptyValue=false,
			value="时区",
			example="GTM-8"
			)
	private String timeZone;
	
	@ApiModelProperty(
			required=false,
			allowEmptyValue=false,
			value="会话ID",
			example="48df415595fb454998ad822a0ce81409"
			)
	private String sessionId;
	
	@ApiModelProperty(
			required=false,
			allowEmptyValue=false,
			value="数字签名,APP对参数生成的验签结果",
			example="sdfgdfghfhfhfghfdg"
			)
	private String sign;
		
	@ApiModelProperty(
			required=false,
			allowEmptyValue=false,
			value="防重复提交码,通过/common/getUniqueToken获取到的结果",
			example="215671"
			)
	private String repeatCode;
	
	@ApiModelProperty(
			required=false,
			allowEmptyValue=false,
			value="时间戳",
			example="1444545656667"
			)
	private Long timestamp;	
	
}
