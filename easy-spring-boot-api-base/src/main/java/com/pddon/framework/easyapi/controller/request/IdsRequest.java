/**  
* Title PaginationRequest.java  
* Description  查询分页请求参数基类
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

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@Accessors(chain=true)
@ApiModel(value="IdsRequest",description="通过某记录的ID获取详细信息请求参数")
public class IdsRequest implements Serializable{

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(
			allowEmptyValue=false,
			value="记录的id集合"
			)
	@NotNull
	private String[] ids;
	
}
