/**  
* Title AbstractCommonExceptionHandler.java  
* Description  
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.exception.handler;

import com.pddon.framework.easyapi.aspect.ApiExceptionAspector;

public abstract class AbstractCommonExceptionHandler implements CommonExceptionHandler {

	public AbstractCommonExceptionHandler(){
		ApiExceptionAspector.addHandler(this);
	}
}
