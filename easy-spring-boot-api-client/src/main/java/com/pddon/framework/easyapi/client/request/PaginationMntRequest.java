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

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@Accessors(chain=true)
public class PaginationMntRequest implements Serializable{

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 获取第几页数据
	 */
	private Long current;
	/**
	 * 每页获取条目总数上限
	 */
	private Long size;
	/**
	 * 按某列排序
	 */
	private String orderBy;
	/**
	 * 是否是升序 true 升序  false 降序
	 * 默认是升序
	 */
	private Boolean isAsc;
	public PaginationMntRequest(){
		this.current=1L;
		this.size=30L;
	}
    private Date lastRecordCrtTime;

	private Integer lastRecordId;
	
}
