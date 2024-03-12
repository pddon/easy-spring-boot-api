/**  
* Title AbstractApiMethodInterceptor.java  
* Description  
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor;


public abstract class AbstractApiMethodInterceptor implements ApiMethodInterceptor {
	
	/**
	 * @author danyuan
	 */
	public AbstractApiMethodInterceptor() {
		ApiInvokeMethodInterceptorManager.addInterceptor(this);
	}
}
