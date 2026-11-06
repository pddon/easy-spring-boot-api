/**  
* Title AbstractFieldDataFormatHandler.java  
* Description  
* @author danyuan
* @date Nov 16, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dataformat;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.pddon.framework.easyapi.interceptor.impl.ApiDataFormatInterceptor;

public abstract class AbstractFieldDataFormatHandler implements FieldDataFormatHandler,
		ApplicationContextAware, InitializingBean, DisposableBean {

	private ApplicationContext applicationContext;

	public AbstractFieldDataFormatHandler(){
		//保持公共注册API的兼容语义：未经Spring容器管理时仍注册到静态集合
		ApiDataFormatInterceptor.addDataFormatHandler(this);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * 由Spring容器创建时，把处理器挂到所属上下文，并从静态集合移除
	 */
	@Override
	public void afterPropertiesSet() {
		if (applicationContext != null) {
			ApiDataFormatInterceptor.registerDataFormatHandler(applicationContext, this);
		}
	}

	@Override
	public void destroy() {
		if (applicationContext != null) {
			ApiDataFormatInterceptor.unregisterDataFormatHandler(applicationContext, this);
		}
	}
	
}
