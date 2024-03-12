/**  
* Title GetUniqueTokenResponse.java  
* Description  
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.response;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString(callSuper=true)
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
public class GetUniqueTokenResponse extends SuccessResponse {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String repeatCode;

}
