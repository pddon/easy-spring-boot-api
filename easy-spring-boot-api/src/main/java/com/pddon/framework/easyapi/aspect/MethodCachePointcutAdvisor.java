/**  
 * Title MethodCachePointcutAdvisor.java  
 * Description  
 * @author danyuan
 * @date Dec 17, 2020
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.annotation.CacheMethodResultEvict;
import com.pddon.framework.easyapi.interceptor.MethodCacheInterceptorManager;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class MethodCachePointcutAdvisor extends
StaticMethodMatcherPointcutAdvisor implements InitializingBean {
	
	@Autowired
	@Lazy
	private MethodCacheInterceptorManager methodCacheInterceptorManager;
	
	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	// 需要拦截的注解
	@SuppressWarnings("unchecked")
	private static final Class<? extends Annotation>[] METHOD_ANNOTATION_CLASSES = new Class[] {
		CacheMethodResult.class, CacheMethodResultEvict.class };

	/**
	 * @param method
	 * @param targetClass
	 * @return
	 * @author danyuan
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		Method m = method;

        if ( isApiAnnotationPresent(m) ) {
            return true;
        }
        
        if ( targetClass != null) {
            try {
                m = targetClass.getMethod(m.getName(), m.getParameterTypes());
                return isApiAnnotationPresent(m);
            } catch (NoSuchMethodException ignored) {
                //default return value is false.  If we can't find the method, then obviously
                //there is no annotation, so just use the default return value.
            }
        }

		return false;
	}

	// 判断目标方法上是否有注解
	private boolean isApiAnnotationPresent(Method method) {
		for (Class<? extends Annotation> annClass : METHOD_ANNOTATION_CLASSES) {
			Annotation a = AnnotationUtils.findAnnotation(method, annClass);
			if (a != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @throws Exception
	 * @author danyuan
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.setAdvice(methodCacheInterceptorManager);
		this.setOrder(LOWEST_PRECEDENCE);
	}

}
