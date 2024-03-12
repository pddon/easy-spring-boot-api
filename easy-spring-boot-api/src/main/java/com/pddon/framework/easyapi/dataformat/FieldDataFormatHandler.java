/**  
* Title FieldDataFormatHandler.java  
* Description  对象字段值加工处理器
* @author danyuan
* @date Nov 16, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dataformat;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface FieldDataFormatHandler {

	/**
	 * @author danyuan
	 */
	int order();
	
	/**
	 * 是否处理请求
	 */
	boolean handleRequest();
	/**
	 * 是否处理响应
	 * @author danyuan
	 */
	boolean handleResponse();
	
	Object handle(String fieldName, Object data, Map<Class<?>, Annotation> annotations) throws Throwable;
	
}
