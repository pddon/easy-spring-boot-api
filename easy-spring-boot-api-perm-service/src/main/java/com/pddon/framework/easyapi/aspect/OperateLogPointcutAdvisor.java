/**  
 * Title OperateLogPointcutAdvisor.java
 * Description  
 * @author danyuan
 * @date Dec 17, 2020
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.aspect;

import com.pddon.framework.easyapi.annotations.OperateLog;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Component
public class OperateLogPointcutAdvisor extends
StaticMethodMatcherPointcutAdvisor implements InitializingBean {
	
	@Autowired
	@Lazy
	private DataPermissionInterceptorManager operateLogInterceptorManager;
	
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

		Annotation a = AnnotationUtils.findAnnotation(method, OperateLog.class);
		if (a != null) {
			return true;
		}
		if(null != AnnotationUtils.findAnnotation(targetClass, OperateLog.class)){
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
		this.setAdvice(operateLogInterceptorManager);
		this.setOrder(LOWEST_PRECEDENCE);
	}

}
