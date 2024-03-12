/**  
* Title ErrorCodeDto.java  
* Description  
* @author danyuan
* @date Nov 8, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ErrorCodeDto implements Serializable{

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String error;

	private String hexCode;

	private Integer code;

	private String msg;
}
