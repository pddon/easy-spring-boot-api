/**  
* Title BusinessErrorDto.java  
* Description  
* @author danyuan
* @date Nov 28, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class BusinessErrorDescription implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private Integer code;
	
	private String desc;
}
