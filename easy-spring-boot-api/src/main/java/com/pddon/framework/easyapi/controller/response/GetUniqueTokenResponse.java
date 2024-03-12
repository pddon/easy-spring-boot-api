/**  
* Title GetUniqueTokenResponse.java  
* Description  
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.controller.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value="获取防重复提交码接口响应参数")
public class GetUniqueTokenResponse extends SuccessResponse {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(
			position=1,
			required=true,
			allowEmptyValue=false,
			notes="防重复提交码，下次提交需要的防重请求时带上此参数",
			example="0"
			)
	private String repeatCode;

}
