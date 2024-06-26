/**  
* Title BusinessException.java  
* Description  业务异常类
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.exception;

import com.pddon.framework.easyapi.component.BusinessErrorCodeInterpreter;
import com.pddon.framework.easyapi.consts.ErrorCodes;

import com.pddon.framework.easyapi.utils.SpringBeanUtil;
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
	public BusinessException(String subCode){
		super(subCode);
		this.subCode=subCode;
		this.code = ErrorCodes.BUSINNES_ERROR.getCode();
		BusinessErrorCodeInterpreter interpreter = SpringBeanUtil.getBean(BusinessErrorCodeInterpreter.class);
		if(interpreter != null){
			this.code= interpreter.getCode(subCode);
		}
	}
	public BusinessException(int code){
		this.code=code;
	}
	public BusinessException(int code,String msg){
		super(msg);
		this.code=code;
		this.msg=msg;
	}
	public BusinessException(int code,String subCode,String msg){
		super(msg);
		this.subCode=subCode;
		this.msg=msg;
		this.code = ErrorCodes.BUSINNES_ERROR.getCode();
		BusinessErrorCodeInterpreter interpreter = SpringBeanUtil.getBean(BusinessErrorCodeInterpreter.class);
		if(interpreter != null){
			this.code= interpreter.getCode(subCode);
		}
	}
	/**
	 * @param error
	 * @author danyuan
	 */
	public BusinessException(ErrorCodes error) {
		super(error.getMsgCode());
		this.code = error.getCode();
		this.msg = error.getMsgCode();
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
