/**  
* Title OperateLogInterceptorManager.java
* Description  
* @author Allen
* @date Dec 17, 2023
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.aspect;

import com.pddon.framework.easyapi.UserOperateLogService;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;


@Component
@Slf4j
public class OperateLogInterceptorManager implements MethodInterceptor {


	@Autowired
	private UserOperateLogService userOperateLogService;
	
	/**
	 * @param invocation
	 * @return
	 * @throws Throwable
	 * @author danyuan
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("进入操作日志记录处理器切面!");
		}
		Object response = null;
		Class<?> targetClass = invocation.getThis().getClass();
		Method method = invocation.getMethod();
		OperateLog operateLog = AnnotationUtils.findAnnotation(targetClass, OperateLog.class);
		if(operateLog == null){
			operateLog = AnnotationUtils.findAnnotation(method, OperateLog.class);
		}
		Date startTime = new Date();
		Boolean complete = true;
		String errorMsg = null;
		try{
			response = invocation.proceed();
		}catch (Exception e){
			complete = false;
			errorMsg = e.getMessage();
			throw e;
		}finally {
			try{
				userOperateLogService.addLog(operateLog.type(), operateLog.apiName(), startTime, complete, errorMsg);
			}catch (Exception e){
				log.warn(IOUtils.getThrowableInfo(e));
			}
		}
		return response;
	}

}
