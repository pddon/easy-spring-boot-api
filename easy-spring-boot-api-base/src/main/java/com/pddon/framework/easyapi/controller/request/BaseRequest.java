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

import com.pddon.framework.easyapi.annotation.IgnoreSign;
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
@ApiModel(value="BaseRequest",description="平台服务请求系统参数,系统参数全部忽略签名")
public class BaseRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//通过网关传递过来的系统参数
	@ApiModelProperty(
			value="APP渠道标识,可通过厂商和机型信息自动关联",
			example="phone-tianlong",
			hidden=true
			)
	@IgnoreSign
	private String appId;
	
	@ApiModelProperty(
			value="当前登录用户的货币类型",
			example="USD",
			hidden=true
			)
	@IgnoreSign
	private String currency;
	@ApiModelProperty(
			value="当前登录用户所属的国家码",
			example="US",
			hidden=true
			)
	@IgnoreSign
	private String countryCode;
	
	@IgnoreSign
	@ApiModelProperty(
			position=5,
			required=false,
			allowEmptyValue=false,
			value="语言",
			example="en_US"
			)
	private String locale;
	
	@IgnoreSign
	@ApiModelProperty(
			position=6,
			required=false,
			allowEmptyValue=false,
			value="时区",
			example="GTM-8"
			)
	private String timeZone;
	
	@IgnoreSign
	@ApiModelProperty(
			position=7,
			required=false,
			allowEmptyValue=false,
			value="会话ID",
			example="48df415595fb454998ad822a0ce81409"
			)
	private String sessionId;
	
	@IgnoreSign
	@ApiModelProperty(
			position=8,
			required=false,
			allowEmptyValue=false,
			value="数字签名,APP对参数生成的验签结果",
			example="sdfgdfghfhfhfghfdg"
			)
	private String sign;
	
	@IgnoreSign
	@ApiModelProperty(
			position=9,
			required=false,
			allowEmptyValue=false,
			value="APP版本号",
			example="90"
			)
	private Integer versionCode;
	
	
	@IgnoreSign
	@ApiModelProperty(
			position=11,
			required=false,
			allowEmptyValue=false,
			value="电信码",
			example="460"
			)
	private String mcc;
	
	@IgnoreSign
	@ApiModelProperty(
			position=12,
			required=false,
			allowEmptyValue=false,
			value="设备IMEI号",
			example="358511020000141"
			)
	private String imei;
	
	
	
	@IgnoreSign
	@ApiModelProperty(
			position=13,
			required=false,
			allowEmptyValue=false,
			value="防重复提交码,通过/common/getUniqueToken获取到的结果",
			example="215671"
			)
	private String repeatCode;
	
	@IgnoreSign
	@ApiModelProperty(
			position=14,
			required=false,
			allowEmptyValue=false,
			value="定位信息 经度",
			example="215671"
			)
	private Double longitude;
	
	@IgnoreSign
	@ApiModelProperty(
			position=15,
			required=false,
			allowEmptyValue=false,
			value="定位信息 纬度",
			example="215671"
			)
	private Double latitude;
	
	@IgnoreSign
	@ApiModelProperty(
			position=16,
			required=false,
			allowEmptyValue=false,
			value="时间戳",
			example="1444545656667"
			)
	private Long timestamp;	
	
}
