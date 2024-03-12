/**  
* Title QueryErrorCodeRequest.java  
* Description  
* @author danyuan
* @date Dec 4, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class QueryErrorCodeRequest implements Serializable {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String error;

	private String hexCode;

	private Integer code;
}
