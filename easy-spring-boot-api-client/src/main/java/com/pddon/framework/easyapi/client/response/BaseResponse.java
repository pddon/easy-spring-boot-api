/**  
* Title BaseResponse.java  
* Description  响应基类，所有返回给客户端的响应都必须继承自此类
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class BaseResponse implements Serializable {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
}
