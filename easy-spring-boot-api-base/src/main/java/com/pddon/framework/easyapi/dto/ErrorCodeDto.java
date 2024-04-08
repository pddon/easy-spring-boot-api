/**  
* Title ErrorCodeDto.java  
* Description  
* @author danyuan
* @date Nov 8, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import com.pddon.framework.easyapi.annotation.LanguageTranslate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.experimental.Accessors;

@JsonInclude(Include.NON_NULL)
@Data
@Accessors(chain = true)
@ApiModel(value="ErrorCodeDto",description="业务错误码参数")
public class ErrorCodeDto implements Serializable{

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(
			value="业务错误码字典值",
			example="user.accountNotFound"
			)
	private String error;
	@ApiModelProperty(
			value="系统错误码整型值对应的十六进制值",
			example="0x06002007"
			)
	private String hexCode;
	@ApiModelProperty(
			value="系统错误码整型值对应的十进制值",
			example="100671495"
			)
	private Integer code;
	
	@ApiModelProperty(
			value="错误描述信息",
			example="该用户账号未找到！"
			)
	@LanguageTranslate
	private String msg;
}
