/**  
* Title AbstractFieldDataFormatHandler.java  
* Description  
* @author danyuan
* @date Nov 16, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dataformat;

import com.pddon.framework.easyapi.interceptor.impl.ApiDataFormatInterceptor;

public abstract class AbstractFieldDataFormatHandler implements FieldDataFormatHandler {

	public AbstractFieldDataFormatHandler(){
		ApiDataFormatInterceptor.addDataFormatHandler(this);
	}
	
}
