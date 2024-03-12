/**  
* Title PaginationRequest.java  
* Description  查询分页请求参数基类
* @author danyuan
* @date Nov 6, 2018
* @version 1.0.0
* blog pddon.cn
*/ 
package com.pddon.framework.easyapi.client.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain=true)
public class PaginationRequest extends BaseRequest {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 获取第几页数据
	 */
	private Integer current;
	/**
	 * 每页获取条目总数上限
	 */
	private Integer size;
	/**
	 * 按某列排序
	 */
	private String orderBy;
	/**
	 * 是否是升序 true 升序  false 降序
	 * 默认是升序
	 */
	private Boolean isAsc;
	public PaginationRequest(){
		this.current=1;
		this.size=8;
		this.isAsc=true;
	}
	
}
