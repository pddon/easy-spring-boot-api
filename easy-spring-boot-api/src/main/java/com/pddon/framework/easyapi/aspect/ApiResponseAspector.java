/**  
 * Title ApiResponseAspector.java  
 * Description  响应内容处理器
 * @author danyuan
 * @date Nov 6, 2018
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.aspect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.interceptor.impl.ApiLogInterceptor;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.response.enhance.ResponseEnhanceHandler;
import com.pddon.framework.easyapi.utils.ContextRegistry;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.pddon.framework.easyapi.utils.ClassOriginCheckUtil;
import com.pddon.framework.easyapi.utils.IOUtils;

@SuppressWarnings("rawtypes")
@RestControllerAdvice
@ControllerAdvice
@Service
@Slf4j
public class ApiResponseAspector implements ResponseBodyAdvice {
	
	/**
	 * 手工注册的响应增强处理器集合（业务通过静态API注册、未经Spring容器管理的实例）
	 */
	private static final List<ResponseEnhanceHandler> handlers = new ArrayList<>();

	/**
	 * 按Spring上下文隔离的处理器注册表
	 */
	private static final ContextRegistry<ResponseEnhanceHandler> CONTEXT_REGISTRY = new ContextRegistry<>();

	private static final Comparator<ResponseEnhanceHandler> ORDER_COMPARATOR = new Comparator<ResponseEnhanceHandler>() {

		@Override
		public int compare(ResponseEnhanceHandler o1, ResponseEnhanceHandler o2) {
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

	@Autowired(required = false)
	private EasyApiConfig config;
	
	@Autowired
	private ApiLogInterceptor apiLogInterceptor;
	
	public static void addResponseEnhanceHandler(ResponseEnhanceHandler handler){
		if(!handlers.contains(handler)){
			handlers.add(handler);
			//添加成功后重新进行排序,使处理器按特定顺序进行增强处理
			handlers.sort(ORDER_COMPARATOR);
		}
	}

	/**
	 * 将Spring容器创建的处理器注册到其所属上下文，并从手工注册集合中移除
	 */
	public static void registerResponseEnhanceHandler(ApplicationContext context, ResponseEnhanceHandler handler){
		CONTEXT_REGISTRY.register(context, handler, handlers, ORDER_COMPARATOR);
	}

	/**
	 * 上下文关闭时注销处理器，避免静态注册表持续增长
	 */
	public static void unregisterResponseEnhanceHandler(ApplicationContext context, ResponseEnhanceHandler handler){
		CONTEXT_REGISTRY.unregister(context, handler);
	}

	/**
	 * 获取指定上下文已注册的处理器快照；没有注册过时返回null
	 */
	static List<ResponseEnhanceHandler> getContextHandlers(ApplicationContext context){
		return CONTEXT_REGISTRY.get(context);
	}

	/**
	 * 解析当前请求应使用的处理器链：优先使用上下文注册的实例，并合并业务手工注册的实例；
	 * 无上下文时沿用静态注册集合，与改造前行为一致。
	 */
	private List<ResponseEnhanceHandler> resolveHandlers(){
		if(handlers.isEmpty()){
			List<ResponseEnhanceHandler> contextHandlers = getContextHandlers(applicationContext);
			if(contextHandlers != null){
				return contextHandlers;
			}
			return handlers;
		}
		List<ResponseEnhanceHandler> contextHandlers = getContextHandlers(applicationContext);
		if(contextHandlers == null || contextHandlers.isEmpty()){
			return handlers;
		}
		List<ResponseEnhanceHandler> merged = new ArrayList<>(handlers);
		merged.addAll(contextHandlers);
		merged.sort(ORDER_COMPARATOR);
		return merged;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
	 * #supports(org.springframework.core.MethodParameter, java.lang.Class)
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class converterType) {
		// 只需要拦截我们配置的包名下的接口即可
		Class<?> targetClass = RequestContext.getContext().getTargetClass();


		return ClassOriginCheckUtil.isBasePackagesChild(targetClass, config.getAllBasePackages());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
	 * #beforeBodyWrite(java.lang.Object,
	 * org.springframework.core.MethodParameter,
	 * org.springframework.http.MediaType, java.lang.Class,
	 * org.springframework.http.server.ServerHttpRequest,
	 * org.springframework.http.server.ServerHttpResponse)
	 */
	@Override
	@ResponseBody
	public Object beforeBodyWrite(Object body, MethodParameter returnType,
			MediaType selectedContentType, Class selectedConverterType,
			ServerHttpRequest request, ServerHttpResponse response) {
		if(log.isTraceEnabled()){
			log.trace("开始处理响应数据：{}", body);
		}
		Object resp = body;
		for(ResponseEnhanceHandler handler : resolveHandlers()){
			resp = handler.enhance(resp);
		}	
		
		try {
			apiLogInterceptor.outputResponseLog(RequestContext.getContext().getApiInfo().getReqParams(),
					resp, RequestContext.getContext().getTargetClass());
		} catch (Throwable e) {
			log.warn(IOUtils.getThrowableInfo(e));
		}
		return resp;
	}

}
