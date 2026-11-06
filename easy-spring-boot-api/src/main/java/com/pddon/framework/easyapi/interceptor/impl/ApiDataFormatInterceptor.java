/**  
* Title ApiDataFormatInterceptor.java  
* Description  接口请求、响应数据加工处理拦截器
* @author danyuan
* @date Nov 15, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.dataformat.FieldDataFormatHandler;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.interceptor.AbstractApiMethodInterceptor;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.ContextRegistry;
import com.pddon.framework.easyapi.utils.IOUtils;

@Service
@Slf4j
public class ApiDataFormatInterceptor extends AbstractApiMethodInterceptor {

	/**
	 * 手工注册的数据格式处理器集合（业务通过静态API注册、未经Spring容器管理的实例）
	 */
	private static final List<FieldDataFormatHandler> handlers = new ArrayList<>();

	/**
	 * 按Spring上下文隔离的处理器注册表
	 */
	private static final ContextRegistry<FieldDataFormatHandler> CONTEXT_REGISTRY = new ContextRegistry<>();

	private static final Comparator<FieldDataFormatHandler> ORDER_COMPARATOR = new Comparator<FieldDataFormatHandler>() {

		@Override
		public int compare(FieldDataFormatHandler o1, FieldDataFormatHandler o2) {
			if(o1.order() > o2.order()){
				return 1;
			}else if(o1.order() < o2.order()){
				return -1;
			}else{
				return 0;
			}
		}
	};

	/**
	 * 当前Bean所属的Spring上下文；手工new（单元测试/非Spring场景）时为null，回退到静态注册集合
	 */
	@Autowired(required = false)
	private ApplicationContext applicationContext;
	
	/**
	 * 方法注解缓存，避免每次响应处理都重复反射获取并构造注解Map。
	 * 按声明类隔离，类卸载时缓存随之释放
	 */
	private static final ClassValue<Map<Method, Map<Class<?>, Annotation>>> METHOD_ANNOTATION_CACHE =
			new ClassValue<Map<Method, Map<Class<?>, Annotation>>>() {
				@Override
				protected Map<Method, Map<Class<?>, Annotation>> computeValue(Class<?> type) {
					return new ConcurrentHashMap<>();
				}
			};
	
	@Autowired
	private LanguageTranslateManager languageTranslateManager;
	
	public static void addDataFormatHandler(FieldDataFormatHandler handler){
		handlers.add(handler);
		//添加成功后重新进行排序,使拦截器按特定顺序进行拦截处理
		handlers.sort(ORDER_COMPARATOR);
	}

	/**
	 * 将Spring容器创建的处理器注册到其所属上下文，并从手工注册集合中移除
	 */
	public static void registerDataFormatHandler(ApplicationContext context, FieldDataFormatHandler handler){
		CONTEXT_REGISTRY.register(context, handler, handlers, ORDER_COMPARATOR);
	}

	/**
	 * 上下文关闭时注销处理器，避免静态注册表持续增长
	 */
	public static void unregisterDataFormatHandler(ApplicationContext context, FieldDataFormatHandler handler){
		CONTEXT_REGISTRY.unregister(context, handler);
	}

	/**
	 * 获取指定上下文已注册的处理器快照；没有注册过时返回null
	 */
	static List<FieldDataFormatHandler> getContextHandlers(ApplicationContext context){
		return CONTEXT_REGISTRY.get(context);
	}

	/**
	 * 解析当前请求应使用的处理器链：优先使用上下文注册的实例，并合并业务手工注册的实例；
	 * 无上下文时沿用静态注册集合，与改造前行为一致。
	 */
	private List<FieldDataFormatHandler> resolveHandlers(){
		if(handlers.isEmpty()){
			List<FieldDataFormatHandler> contextHandlers = getContextHandlers(applicationContext);
			if(contextHandlers != null){
				return contextHandlers;
			}
			return handlers;
		}
		List<FieldDataFormatHandler> contextHandlers = getContextHandlers(applicationContext);
		if(contextHandlers == null || contextHandlers.isEmpty()){
			return handlers;
		}
		List<FieldDataFormatHandler> merged = new ArrayList<>(handlers);
		merged.addAll(contextHandlers);
		merged.sort(ORDER_COMPARATOR);
		return merged;
	}
	/**
	 * @author danyuan
	 */
	@Override
	public int order() {
		return -2;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void preInvoke(List<ApiRequestParameter> requestParams,
			Method method, Class<?> targetClass) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("开始对请求内容进行密文解密！");
		}
		if(requestParams != null && requestParams.size() > 0){
			final List<FieldDataFormatHandler> currentHandlers = resolveHandlers();
			for(ApiRequestParameter parameter : requestParams){//每个参数的每个子字段都递归检查一遍
				Object obj = parameter.getParam();
				Map<Class<?>, Annotation> annos = new HashMap<>();
				if(parameter.getAnnotations() != null){
					for(Annotation anno : parameter.getAnnotations()){
						annos.put(anno.annotationType(), anno);
					}
				}					
				obj = BeanPropertyUtil.decorateObj(parameter.getParamName(), obj, annos,  (fieldName, data, annotations)->{
					for(FieldDataFormatHandler handler : currentHandlers){
						if(handler.handleRequest()){
							try{
								data = handler.handle(fieldName, data, annotations);
							}catch(BusinessException e){
								log.warn(languageTranslateManager.get(e.getMessage(), e.getParams()));
							}catch(Exception e){
								log.warn(IOUtils.getThrowableInfo(e));
							}							
						}						
					}						
					return data;
				});
				parameter.getArgs()[parameter.getIndex()] = obj;//改变参数值
				parameter.setParam(obj);
			}
		}	
	}

	/**
	 * @author danyuan
	 */
	@Override
	public Object afterInvoke(List<ApiRequestParameter> requestParams,
			Object resp, Method method, Class<?> targetClass) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("开始对响应内容进行明文加密！");
		}
		Map<Method, Map<Class<?>, Annotation>> annotationCache = METHOD_ANNOTATION_CACHE.get(method.getDeclaringClass());
		final List<FieldDataFormatHandler> currentHandlers = resolveHandlers();
		Map<Class<?>, Annotation> annos = annotationCache.get(method);
		if(annos == null){
			annos = new HashMap<>();
			if(method.getAnnotations() != null){
				for(Annotation anno : method.getAnnotations()){
					annos.put(anno.annotationType(), anno);
				}
			}
			Map<Class<?>, Annotation> cached = annotationCache.putIfAbsent(method, annos);
			if(cached != null){
				annos = cached;
			}
		}
		resp = BeanPropertyUtil.decorateObj("resp", resp, annos,  (fieldName, data,annotations)->{
			for(FieldDataFormatHandler handler : currentHandlers){
				if(handler.handleResponse()){
					try{
						data = handler.handle(fieldName, data, annotations);
					}catch(BusinessException e){
						log.warn(languageTranslateManager.get(e.getMessage(), e.getParams()));
					}catch(Exception e){
						log.warn(IOUtils.getThrowableInfo(e));
					}	
				}				
			}				
			return data;
		});
		return resp;
	}

}
