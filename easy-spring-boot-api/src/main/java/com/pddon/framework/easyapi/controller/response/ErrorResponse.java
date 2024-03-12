/**  
 * Title ErrorResponse.java  
 * Description  错误响应
 * @author danyuan
 * @date Nov 6, 2018
 * @version 1.0.0
 * blog pddon.cn
 */
package com.pddon.framework.easyapi.controller.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse extends BaseResponse {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 错误信息详情
	 */
	List<ErrorInfo> subErrors;
	
}
