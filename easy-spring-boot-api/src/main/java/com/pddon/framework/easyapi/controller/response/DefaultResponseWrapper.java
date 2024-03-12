/**  
* Title BaseResponse.java  
* Description  响应包装类
* 兼容之前老接口
* @author danyuan
* @date Nov 6, 2018
* @version 1.0.0
* blog pddon.cn
*/ 
package com.pddon.framework.easyapi.controller.response;

import java.io.Serializable;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString(callSuper=true)
@Accessors(chain=true)
public class DefaultResponseWrapper<T> implements Serializable {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private Integer code;
	private String subCode;
	private String msg;
	private T data;
	private String sign;
	private Long timestamp;
	/**
	 * 预留出来的可拓展系统参数字段
	 */
	private Map<String,Object> otherParams;
	
	
	public DefaultResponseWrapper(T data){
		this.code=ErrorCodes.SUCCESS.getCode();
		this.data=data;
	}
	public DefaultResponseWrapper(Integer code,String msg){
		this.code=code;
		this.msg=msg;
	}
	public DefaultResponseWrapper(Integer code,String msg,T body){
		this.code=code;
		this.msg=msg;
		this.data=body;
	}
	public DefaultResponseWrapper(){
		this.code=ErrorCodes.SUCCESS.getCode();
	}
	public DefaultResponseWrapper(ErrorCodes error){
		this.code=error.getCode();
		this.msg=error.getMsgCode();
	}
	public DefaultResponseWrapper(BusinessException e){
		this.code=e.getCode();
		this.subCode = e.getSubCode();
		this.msg=e.getMsg();
	}
	
}
