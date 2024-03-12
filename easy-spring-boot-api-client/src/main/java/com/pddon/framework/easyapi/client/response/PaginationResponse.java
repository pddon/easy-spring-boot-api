/**  
* Title PaginationResponse.java  
* Description  
* @author danyuan
* @date May 11, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
@Data
@Accessors(chain = true)
public class PaginationResponse<T> implements Serializable{
	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private List<T> records = Collections.emptyList();;
    private Long total = 0L;
    private Long size = 0L;
    private Long current = 1L;
	private Long pages = 0L;
}
