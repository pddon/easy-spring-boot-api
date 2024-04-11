/**  
* Title IgnoreTenantInterceptorManager.java
* Description  
* @author Allen
* @date Dec 17, 2023
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dao.tenant;

import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


@Component
@Slf4j
public class IgnoreTenantInterceptorManager implements MethodInterceptor {

	/**
	 * @param invocation
	 * @return
	 * @throws Throwable
	 * @author danyuan
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("进入忽略多租户查询处理器切面!");
		}
		Class<?> targetClass = invocation.getThis().getClass();	
		Method method = invocation.getMethod();
		if( AnnotationUtils.findAnnotation(targetClass, IgnoreTenant.class) != null
				|| (AnnotationUtils.findAnnotation(method, IgnoreTenant.class) != null)){
			RequestContext.getContext().setIgnoreTenant(true);
		}
		try{
			return invocation.proceed();
		}finally {
			RequestContext.getContext().setIgnoreTenant(false);
		}
	}

}
