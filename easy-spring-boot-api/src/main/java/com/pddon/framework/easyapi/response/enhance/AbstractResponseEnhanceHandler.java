/**  
* Title AbstractResponseEnhanceHandler.java  
* Description  
* @author danyuan
* @date Nov 8, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.response.enhance;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.pddon.framework.easyapi.aspect.ApiResponseAspector;

public abstract class AbstractResponseEnhanceHandler implements ResponseEnhanceHandler,
		ApplicationContextAware, InitializingBean, DisposableBean {

	private ApplicationContext applicationContext;

	public AbstractResponseEnhanceHandler(){
		//保持公共注册API的兼容语义：未经Spring容器管理时仍注册到静态集合
		ApiResponseAspector.addResponseEnhanceHandler(this);
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
			ApiResponseAspector.registerResponseEnhanceHandler(applicationContext, this);
		}
	}

	@Override
	public void destroy() {
		if (applicationContext != null) {
			ApiResponseAspector.unregisterResponseEnhanceHandler(applicationContext, this);
		}
	}
}
