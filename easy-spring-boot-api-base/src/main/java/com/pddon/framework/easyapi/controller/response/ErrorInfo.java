/**  
* Title ErrorInfo.java  
* Description  详细错误信息
* @author danyuan
* @date Nov 6, 2018
* @version 1.0.0
* blog pddon.cn
*/ 
package com.pddon.framework.easyapi.controller.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
public class ErrorInfo implements Serializable {
	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 错误码标识
	 */
	private String error;
	/**
	 * 错误描述信息
	 */
	private String msg;

}
