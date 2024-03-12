/**  
* Title ItemResponse.java  
* Description  
* @author danyuan
* @date Jan 7, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString(callSuper=true)
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse<T> extends SuccessResponse {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private T item;
	
}
