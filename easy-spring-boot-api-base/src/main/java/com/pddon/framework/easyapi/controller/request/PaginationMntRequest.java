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
import java.util.Date;

import com.pddon.framework.easyapi.json.parse.DateToGTM0SecondString;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain=true)
@ApiModel(value="PaginationMntRequest",description="分页请求参数")
public class PaginationMntRequest implements Serializable{

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 获取第几页数据
	 */
	@ApiModelProperty(
			position=1,
			allowEmptyValue=false,
			value="获取第几页数据"
			)
	private Long current;
	/**
	 * 每页获取条目总数上限
	 */
	@ApiModelProperty(
			position=2,
			allowEmptyValue=false,
			value="每页获取条目总数上限"
			)
	private Long size;
	/**
	 * 按某列排序
	 */
	@ApiModelProperty(
			position=3,
			allowEmptyValue=false,
			value="按某列排序"
			)
	private String orderBy;
	/**
	 * 是否是升序 true 升序  false 降序
	 * 默认是升序
	 */
	@ApiModelProperty(
			position=4,
			allowEmptyValue=false,
			value="是否是升序 true 升序  false 降序,默认是升序"
			)
	private Boolean isAsc;
	public PaginationMntRequest(){
		this.current=1L;
		this.size=30L;
	}
	
	@ApiModelProperty(
			allowEmptyValue=false,
			value="分页时，传递前一页最后一条记录的创建时间，优化查询性能"
			)
    @JsonFormat(pattern = DateToGTM0SecondString.PARTNER)
    private Date lastRecordCrtTime;
	
	@ApiModelProperty(
			allowEmptyValue=false,
			value="分页时，传递前一页最后一条记录的ID，优化查询性能"
			)
	private Integer lastRecordId;
	
}
