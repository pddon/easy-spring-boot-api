/**  
* Title PaginationResponse.java  
* Description  
* @author danyuan
* @date May 11, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.controller.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
@Data
@Accessors(chain = true)
@ApiModel(value="PaginationResponse",description="平台服务分页响应参数")
public class PaginationResponse<T> implements Serializable{
	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(
			allowEmptyValue=false,
			value="记录项列表"
			)
	private List<T> records = Collections.emptyList();;
	@ApiModelProperty(
			allowEmptyValue=false,
			value="总条数"
			)
    private Long total = 0L;
	@ApiModelProperty(
			allowEmptyValue=false,
			value="每页最大条数"
			)
    private Long size = 0L;
	@ApiModelProperty(
			allowEmptyValue=false,
			value="当前页码值"
			)
    private Long current = 1L;
	@ApiModelProperty(
			allowEmptyValue=false,
			value="满足条件的总页码数"
			)
	private Long pages = 0L;
}
