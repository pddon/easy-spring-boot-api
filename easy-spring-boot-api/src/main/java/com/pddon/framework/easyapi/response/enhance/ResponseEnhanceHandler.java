/**  
* Title ResponseEnhanceHandler.java  
* Description  响应包装处理器，对响应进行额外处理，例如加外壳，添加数字签名等等
* @author danyuan
* @date Nov 8, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.response.enhance;


public interface ResponseEnhanceHandler {
	
	int order();

	Object enhance(Object resp);
}
