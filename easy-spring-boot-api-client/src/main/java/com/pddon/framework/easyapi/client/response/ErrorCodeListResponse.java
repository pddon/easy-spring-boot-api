/**  
* Title ErrorCodeListResponse.java  
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
import java.util.List;
@Data
@Accessors(chain = true)
public class ErrorCodeListResponse implements Serializable {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private List<ErrorCodeDto> bussinessErrors;

	private List<ErrorCodeDto> systemErrors;
}
