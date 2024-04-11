/**  
 * Title IgnoreTenantAdvisor.java
 * Description  
 * @author Allen
 * @date Dec 17, 2023
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.dao.tenant;

import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Component
public class IgnoreTenantAdvisor extends
StaticMethodMatcherPointcutAdvisor implements InitializingBean {
	
	@Autowired
	@Lazy
	private IgnoreTenantInterceptorManager ignoreTenantInterceptorManager;
	
	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param method
	 * @param targetClass
	 * @return
	 * @author Allen
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		if( AnnotationUtils.findAnnotation(targetClass, IgnoreTenant.class) != null
				|| (AnnotationUtils.findAnnotation(method, IgnoreTenant.class) != null)){
			return true;
		}
		return false;
	}

	/**
	 * @throws Exception
	 * @author Allen
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.setAdvice(ignoreTenantInterceptorManager);
		this.setOrder(HIGHEST_PRECEDENCE);
	}

}
