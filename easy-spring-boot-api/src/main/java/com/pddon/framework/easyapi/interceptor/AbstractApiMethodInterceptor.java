/**  
* Title AbstractApiMethodInterceptor.java  
* Description  
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class AbstractApiMethodInterceptor implements ApiMethodInterceptor,
		ApplicationContextAware, InitializingBean, DisposableBean {

	private ApplicationContext applicationContext;

	/**
	 * @author danyuan
	 */
	public AbstractApiMethodInterceptor() {
		//保持公共注册API的兼容语义：未经Spring容器管理时仍注册到静态集合
		ApiInvokeMethodInterceptorManager.addInterceptor(this);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * 由Spring容器创建时，把实例挂到所属上下文，并从静态集合移除以避免跨上下文驻留
	 */
	@Override
	public void afterPropertiesSet() {
		if (applicationContext != null) {
			ApiInvokeMethodInterceptorManager.registerInterceptor(applicationContext, this);
		}
	}

	/**
	 * 上下文关闭时注销，避免静态注册表持有已销毁的实例
	 */
	@Override
	public void destroy() {
		if (applicationContext != null) {
			ApiInvokeMethodInterceptorManager.unregisterInterceptor(applicationContext, this);
		}
	}
}
