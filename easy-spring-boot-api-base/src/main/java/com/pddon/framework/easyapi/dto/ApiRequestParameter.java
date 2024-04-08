/**  
* Title ApiRequestParameter.java  
* Description  
* @author danyuan
* @date Nov 14, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import lombok.Data;

@Data
public class ApiRequestParameter implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 请求参数的下标
	 */
	private int index;
	/**
	 * 参数名
	 */
	private String paramName;
	/**
	 * 参数值
	 */
	private Object param;
	/**
	 * 是否需要签名
	 */
	private boolean needSign = true;
	/**
	 * 该请求参数是否需要解密
	 */
	private boolean decrpt;
	/**
	 * 参数上的注解
	 */
	private Annotation[] annotations;
	
	private Object[] args;
}
