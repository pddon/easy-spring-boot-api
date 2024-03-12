/**  
* Title ListResponse.java  
* Description  
* @author danyuan
* @date Jan 7, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.response;


import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString(callSuper=true)
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
public class ListResponse<T> extends SuccessResponse {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private List<T> list;
	
}
