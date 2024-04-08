/**  
* Title ErrorCodeListResponse.java  
* Description  
* @author danyuan
* @date Nov 8, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.controller.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

import com.pddon.framework.easyapi.dto.ErrorCodeDto;

import lombok.Data;
import lombok.experimental.Accessors;
@Data
@Accessors(chain = true)
@ApiModel(value="ErrorCodeListResponse",description="获取系统错误码信息的返回参数")
public class ErrorCodeListResponse implements Serializable {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(
			value="业务错误列表"
			)
	private List<ErrorCodeDto> bussinessErrors;
	@ApiModelProperty(
			value="系统错误列表"
			)
	private List<ErrorCodeDto> systemErrors;
}
