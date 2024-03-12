/**  
* Title MapResponse.java  
* Description  
* @author danyuan
* @date Jul 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.response;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Setter
@ToString(callSuper=true)
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
public class MapResponse<T,U> extends SuccessResponse {/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<T,U> map;
}
