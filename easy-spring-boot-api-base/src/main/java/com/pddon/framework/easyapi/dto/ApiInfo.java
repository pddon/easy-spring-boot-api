/**  
* Title ApiInfo.java  
* Description  
* @author danyuan
* @date Oct 31, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ApiInfo implements Serializable {/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 接口名
	 */
	private String apiName;
	/**
	 * 接口URI
	 */
	private String apiUri;
	/**
	 * http请求类型
	 */
	private String apiMethod;
	/**
	 * 接口版本
	 */
	private String apiVersion;	
	/**
	 * 请求参数
	 */
	private List<ApiRequestParameter> reqParams;
	/**
	 * 响应结果
	 */
	private Object response;
}
