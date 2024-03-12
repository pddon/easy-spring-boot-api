/**  
* Title BusinessException.java  
* Description  业务异常类
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class BusinessException extends RuntimeException {
	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 系统异常码
	 */
	private int code;
	/**
	 * 业务异常码
	 */
	private String subCode;
	/**
	 * 参数
	 */
	private Object[] params;
	/**
	 * 描述信息，可以打印到日志中
	 */
	private String msg;
	public BusinessException(){
		
	}

	public BusinessException(int code){
		this.code=code;
	}
	public BusinessException(int code, String msg){
		super(msg);
		this.code=code;
		this.msg=msg;
	}

	public BusinessException(String msg){
		super(msg);
		this.code=-1;
		this.msg=msg;
	}

	public BusinessException(int code, String subCode, String msg){
		super(msg);
		this.code=code;
		this.subCode=subCode;
		this.msg=msg;
	}

	public Object[] getParams() {
		return params;
	}
	public BusinessException setParams(Object[] params) {
		this.params = params;
		return this;
	}
	public Object getParam() {
		if(params!=null&&(params.length>0))
			return params[0];
		return null;
	}
	public BusinessException setParam(String... params) {
		this.params = params;
		return this;
	}
	
}
