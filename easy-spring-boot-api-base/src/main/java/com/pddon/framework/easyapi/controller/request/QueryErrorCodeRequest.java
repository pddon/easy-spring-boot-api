/**  
* Title QueryErrorCodeRequest.java  
* Description  
* @author danyuan
* @date Dec 4, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.controller.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value="QueryErrorCodeRequest",description="查询某个错误码的信息")
public class QueryErrorCodeRequest implements Serializable {

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
}
