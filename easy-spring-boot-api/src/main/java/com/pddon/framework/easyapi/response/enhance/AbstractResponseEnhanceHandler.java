/**  
* Title AbstractResponseEnhanceHandler.java  
* Description  
* @author danyuan
* @date Nov 8, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.response.enhance;

import com.pddon.framework.easyapi.aspect.ApiResponseAspector;

public abstract class AbstractResponseEnhanceHandler implements ResponseEnhanceHandler {

	public AbstractResponseEnhanceHandler(){
		ApiResponseAspector.addResponseEnhanceHandler(this);
	}
}
