/**  
* Title MethodCacheInterceptorManager.java  
* Description  
* @author danyuan
* @date Dec 17, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.annotation.CacheMethodResultEvict;
import com.pddon.framework.easyapi.cache.MethodCacheManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.SpringBeanUtil;
import com.pddon.framework.easyapi.utils.StringUtils;


@Component
@Slf4j
public class MethodCacheInterceptorManager implements MethodInterceptor {

	@Autowired
	@Lazy
	private MethodCacheManager methodCacheManager;
	
	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
	
	/**
	 * @param invocation
	 * @return
	 * @throws Throwable
	 * @author danyuan
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("进入方法缓存处理器切面!");
		}
		Class<?> targetClass = invocation.getThis().getClass();	
		Object response = null;
		Object [] args=invocation.getArguments();		
		Method method = invocation.getMethod();
		String[] parameters = parameterNameDiscoverer.getParameterNames(method);    
	   
        //获取缓存注解信息
		CacheManager cacheManager = null;
		CacheMethodResult cacheMethodResult = AnnotationUtils.findAnnotation(method, CacheMethodResult.class);
		CacheMethodResultEvict cacheMethodResultEvict = AnnotationUtils.findAnnotation(method, CacheMethodResultEvict.class);
		if(cacheMethodResult != null){
			//获取缓存key
			String cacheKey = methodCacheManager.getCacheKey(cacheMethodResult.prefix(), cacheMethodResult.keyMode(), cacheMethodResult.id(), parameters, args, targetClass, method);
			String needCacheFlag = methodCacheManager.getFieldValue(cacheMethodResult.needCacheField(), parameters, args, method);
			if(StringUtils.isNotEmpty(needCacheFlag) && needCacheFlag.equalsIgnoreCase(Boolean.FALSE.toString())){
				//关闭缓存功能
				if(log.isTraceEnabled()){
					log.trace("缓存方法{}的执行结果功能已关闭，直接执行业务!", cacheKey);
				}
				return invocation.proceed();
			}
			//缓存
			cacheManager = SpringBeanUtil.getBean(cacheMethodResult.cacheManager());
			if(cacheManager == null){
				throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam(cacheMethodResult.cacheManager().toString());
			}

			if(StringUtils.isNotEmpty(cacheKey)){
				//检查是否存在方法结果缓存
				response = methodCacheManager.getResult(cacheKey, cacheManager, cacheMethodResult.expireSeconds(), cacheMethodResult.expireMode());
				if(response != null){
					//存在则直接返回缓存结果
					if(log.isTraceEnabled()){
						log.trace("命中缓存[{}]，直接返回缓存结果!", cacheKey);
					}
					return response;
				}
			}
			//否则直接执行正常业务逻辑
			if(log.isTraceEnabled()){
				log.trace("未命中缓存[{}]，执行业务流程!", cacheKey);
			}
			response = invocation.proceed();
			if(StringUtils.isNotEmpty(response) && StringUtils.isNotEmpty(cacheKey)){
				//缓存方法返回的结果
				methodCacheManager.cacheResult(cacheKey, response, cacheManager, cacheMethodResult.expireSeconds(), cacheMethodResult.expireMode());
			}			
		}else if(cacheMethodResultEvict != null){
			//先执行业务流程
			response = invocation.proceed();
			//执行成功后删除前一次的缓存结果
			cacheManager = SpringBeanUtil.getBean(cacheMethodResultEvict.cacheManager());
			if(cacheManager == null){
				throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam(cacheMethodResultEvict.cacheManager().toString());
			}
			//获取缓存key
			String cacheKey = methodCacheManager.getCacheKey(cacheMethodResultEvict.prefix(), cacheMethodResultEvict.keyMode(), cacheMethodResultEvict.id(), parameters, args, targetClass, method);
			if(StringUtils.isNotEmpty(cacheKey)){
				if(log.isTraceEnabled()){
					log.trace("检查并清理缓存[{}]", cacheKey);
				}
				methodCacheManager.remove(cacheKey, cacheManager, cacheMethodResultEvict.expireSeconds(), cacheMethodResultEvict.expireMode());
			}
		}else{
			log.warn("未找到缓存相关注解，请检查配置！");
			response = invocation.proceed();
		}
		
		return response;
	}

}
