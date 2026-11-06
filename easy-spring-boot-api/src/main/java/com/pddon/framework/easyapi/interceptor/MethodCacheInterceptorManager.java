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
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.pddon.framework.easyapi.properties.EasyApiConfig;
import lombok.extern.slf4j.Slf4j;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
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

	@Autowired
	@Lazy
	private EasyApiConfig easyApiConfig;
	
	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
	/**
	 * 方法参数名缓存，避免每次调用都解析字节码的LocalVariableTable。
	 * 按声明类隔离，类卸载时缓存随之释放
	 */
	private static final ClassValue<ConcurrentHashMap<Method, String[]>> PARAMETER_NAMES_CACHE =
			new ClassValue<ConcurrentHashMap<Method, String[]>>() {
				@Override
				protected ConcurrentHashMap<Method, String[]> computeValue(Class<?> type) {
					return new ConcurrentHashMap<>();
				}
			};
	
	/**
	 * 解析方法缓存TTL：配置easyapi.method-cache.expire-seconds-by-prefix中存在该前缀时优先使用配置值，
	 * 否则沿用注解值（默认空配置，行为与改造前完全一致）。
	 * 缓存写入与清理共用同一解析结果，保证key所在TTL桶一致。
	 */
	long resolveExpireSeconds(String prefix, long annotationExpireSeconds){
		if(StringUtils.isEmpty(prefix) || easyApiConfig == null || easyApiConfig.getMethodCache() == null){
			return annotationExpireSeconds;
		}
		Map<String, Long> overrides = easyApiConfig.getMethodCache().getExpireSecondsByPrefix();
		if(overrides == null || overrides.isEmpty()){
			return annotationExpireSeconds;
		}
		Long configured = overrides.get(prefix);
		return configured != null && configured > 0 ? configured : annotationExpireSeconds;
	}

	/**
	 * 是否使用新格式的自动参数缓存key（easyapi.method-cache.auto-key-v2，默认false）
	 */
	private boolean isAutoKeyV2(){
		return easyApiConfig != null && easyApiConfig.getMethodCache() != null && easyApiConfig.getMethodCache().isAutoKeyV2();
	}

	/**
	 * @param invocation
	 * @return
	 * @throws Throwable
	 * @author danyuan
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if(easyApiConfig.getDisableMethodCache() == true){
			return invocation.proceed();
		}
		if(AopUtils.isAopProxy(invocation.getThis())){
			return invocation.proceed();
		}
		if(log.isTraceEnabled()){
			log.trace("进入方法缓存处理器切面!");
		}
		Class<?> tClass = invocation.getThis().getClass();
		try{
			tClass = AopProxyUtils.ultimateTargetClass(invocation.getThis());
		}catch (Exception e){}
		final Class<?> targetClass = tClass;
		Object response = null;
		Object [] args=invocation.getArguments();		
		Method method = invocation.getMethod();
		Class<?> returnType = method.getReturnType();
		ConcurrentHashMap<Method, String[]> parameterNamesCache = PARAMETER_NAMES_CACHE.get(method.getDeclaringClass());
		String[] discoveredParameters = parameterNamesCache.get(method);
		if(discoveredParameters == null){
			discoveredParameters = parameterNameDiscoverer.getParameterNames(method);
			if(discoveredParameters != null){
				String[] cached = parameterNamesCache.putIfAbsent(method, discoveredParameters);
				if(cached != null){
					discoveredParameters = cached;
				}
			}
		}
		//保持lambda表达式可捕获（必须为effectively final）
		final String[] parameters = discoveredParameters;

	   
        //获取缓存注解信息
		CacheManager cacheManager = null;
		CacheMethodResult cacheMethodResult = AnnotationUtils.findAnnotation(method, CacheMethodResult.class);
		CacheMethodResultEvict cacheMethodResultEvict = AnnotationUtils.findAnnotation(method, CacheMethodResultEvict.class);
		if(cacheMethodResult != null){
			//获取缓存key
			String cacheKey = methodCacheManager.getCacheKey(cacheMethodResult.prefix(), cacheMethodResult.keyMode(), cacheMethodResult.id(), parameters, args, targetClass, method, isAutoKeyV2());
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

			long expireSeconds = resolveExpireSeconds(cacheMethodResult.prefix(), cacheMethodResult.expireSeconds());
			if(StringUtils.isNotEmpty(cacheKey)){
				//检查是否存在方法结果缓存
				response = methodCacheManager.getResult(cacheKey, returnType, cacheManager, expireSeconds, cacheMethodResult.expireMode());
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
				methodCacheManager.cacheResult(cacheKey, response, cacheManager, expireSeconds, cacheMethodResult.expireMode());
			}			
		}else if(cacheMethodResultEvict != null){
			//先执行业务流程
			response = invocation.proceed();

			if(cacheMethodResultEvict.prefix().length > 0){
				Arrays.stream(cacheMethodResultEvict.prefix()).forEach(prefix -> {
					//执行成功后删除前一次的缓存结果
					CacheManager cm = SpringBeanUtil.getBean(cacheMethodResultEvict.cacheManager());
					if(cm == null){
						throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam(cacheMethodResultEvict.cacheManager().toString());
					}
					//获取缓存key
					String cacheKey = methodCacheManager.getCacheKey(prefix, cacheMethodResultEvict.keyMode(), cacheMethodResultEvict.id(), parameters, args, targetClass, method, isAutoKeyV2());
					if(StringUtils.isNotEmpty(cacheKey)){
						if(log.isTraceEnabled()){
							log.trace("检查并清理缓存[{}]", cacheKey);
						}
						methodCacheManager.remove(cacheKey, cm, resolveExpireSeconds(prefix, cacheMethodResultEvict.expireSeconds()), cacheMethodResultEvict.expireMode());
					}
				});
			}
		}else{
			log.warn("未找到缓存相关注解，请检查配置！");
			response = invocation.proceed();
		}
		
		return response;
	}

}
