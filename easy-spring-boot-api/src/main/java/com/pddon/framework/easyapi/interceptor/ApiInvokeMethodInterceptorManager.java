/**  
* Title ApiInvokeMethodInterceptorManager.java  
* Description  
* @author danyuan
* @date Dec 17, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.pddon.framework.easyapi.interceptor.impl.ApiLogInterceptor;
import com.pddon.framework.easyapi.interceptor.impl.NoRepeatSubmitInterceptor;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.utils.ContextRegistry;
import lombok.extern.slf4j.Slf4j;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import com.pddon.framework.easyapi.aspect.ApiExceptionAspector;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiInfo;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.utils.ClassOriginCheckUtil;
import com.pddon.framework.easyapi.utils.MethodInvokeUtil;

@Component
@Slf4j
public class  ApiInvokeMethodInterceptorManager implements MethodInterceptor {

	private static String DEFAULT_API_VERSION = "1.0";
	private static String API_NAME = "%s::%s";
	private static Pattern PATTERN = Pattern.compile("[0-9\\.]*");
	
	@Autowired
	@Lazy
	private ApiLogInterceptor apiLogInterceptor;
	
	@Autowired
	@Lazy
	private ApiExceptionAspector apiExceptionAspector;
	
	@Autowired
	@Lazy
	private NoRepeatSubmitInterceptor noRepeatSubmitInterceptor;
	
	@Autowired
	@Lazy
	private EasyApiConfig easyApiConfig;
	
	/**
	 * 手工注册的拦截器集合（业务通过静态API注册、未经Spring容器管理的实例）。
	 * 由Spring容器创建的拦截器会在初始化完成后迁移到按上下文隔离的注册表，
	 * 不再长期驻留于此，避免多上下文场景下旧上下文的实例被新的请求链路复用。
	 */
	private static final List<ApiMethodInterceptor> interceptors = new ArrayList<>();

	/**
	 * 按Spring上下文隔离的拦截器注册表，上下文关闭时由Bean销毁回调清理
	 */
	private static final ContextRegistry<ApiMethodInterceptor> CONTEXT_REGISTRY = new ContextRegistry<>();

	private static final Comparator<ApiMethodInterceptor> ORDER_COMPARATOR = new Comparator<ApiMethodInterceptor>() {

		@Override
		public int compare(ApiMethodInterceptor o1, ApiMethodInterceptor o2) {
			if (o1.order() > o2.order()) {
				return 1;
			} else if (o1.order() < o2.order()) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	/**
	 * 当前Bean所属的Spring上下文；手工new（单元测试/非Spring场景）时为null，此时回退到静态注册集合
	 */
	@Autowired(required = false)
	private ApplicationContext applicationContext;

	/**
	 * 业务调用失败标记。
	 * 仅在拦截器finally链内用于告知防重复提交拦截器“本次业务未成功”，
	 * 该标记是不可变单例，不携带任何请求状态，不会跨线程串值。
	 */
	private static final Object INVOKE_FAILED_MARKER = new Object();

	/**
	 * 判断afterInvoke收到的响应是否为业务调用失败标记
	 */
	public static boolean isInvokeFailedMarker(Object response){
		return response == INVOKE_FAILED_MARKER;
	}
	private static ParameterNameDiscoverer parameterNameDiscoverer = new PrioritizedParameterNameDiscoverer();
	/**
	 * 方法元数据缓存：参数名、方法注解、参数注解在运行期不会变化，缓存后可避免每次请求重复反射。
	 * 使用ClassValue按声明类隔离，类卸载时缓存随之释放，避免静态缓存持有类加载器。
	 */
	private static final ClassValue<ConcurrentHashMap<Method, MethodMeta>> METHOD_META_CACHE =
			new ClassValue<ConcurrentHashMap<Method, MethodMeta>>() {
				@Override
				protected ConcurrentHashMap<Method, MethodMeta> computeValue(Class<?> type) {
					return new ConcurrentHashMap<>();
				}
			};
	/**
	 * 是否需要在当前方法上执行API拦截的逻辑缓存，避免每次请求重复做注解与包名判断。
	 * 同样按声明类隔离，并记录根包集合指纹，根包配置变化后自动失效。
	 */
	private static final ClassValue<InterceptCache> INTERCEPT_CACHE = new ClassValue<InterceptCache>() {
		@Override
		protected InterceptCache computeValue(Class<?> type) {
			return new InterceptCache();
		}
	};

	static final class MethodMeta {
		final String[] paramNames;
		final Annotation[] methodAnnos;
		final Annotation[][] parameterAnnotations;

		private MethodMeta(String[] paramNames, Annotation[] methodAnnos, Annotation[][] parameterAnnotations){
			this.paramNames = paramNames;
			this.methodAnnos = methodAnnos;
			this.parameterAnnotations = parameterAnnotations;
		}
	}

	/**
	 * 单个声明类上的拦截判断缓存，根包集合内容变化时整体失效
	 */
	private static final class InterceptCache {
		/**
		 * 保存根包集合快照（不可变副本），用于精确比较内容而不是只比较hashCode，避免哈希碰撞导致误用旧结果
		 */
		private volatile Set<String> basePackages;
		private volatile ConcurrentHashMap<Method, Boolean> results = new ConcurrentHashMap<>();

		private void ensurePackages(Set<String> packages) {
			Set<String> current = this.basePackages;
			if (current != null && current.equals(packages)) {
				return;
			}
			synchronized (this) {
				current = this.basePackages;
				if (current == null || !current.equals(packages)) {
					this.results = new ConcurrentHashMap<>();
					this.basePackages = Collections.unmodifiableSet(new HashSet<>(packages));
				}
			}
		}
	}
	
	static {
		PrioritizedParameterNameDiscoverer discoverer = (PrioritizedParameterNameDiscoverer)parameterNameDiscoverer;
		// 1. 首先尝试 Java 8+ 反射（需要 -parameters 编译参数）
		discoverer.addDiscoverer(new StandardReflectionParameterNameDiscoverer());

		// 2. 然后尝试读取字节码的 LocalVariableTable（需要 -g 调试信息）
		discoverer.addDiscoverer(new LocalVariableTableParameterNameDiscoverer());

		// 3. 最后使用 ASM 字节码分析
		discoverer.addDiscoverer(new KotlinReflectionParameterNameDiscoverer());
	}

	/**
	 * 添加接口拦截器
	 * @author danyuan
	 */
	public static void addInterceptor(ApiMethodInterceptor interceptor){
		interceptors.add(interceptor);
		//添加成功后重新进行排序,使拦截器按特定顺序进行拦截处理
		interceptors.sort(ORDER_COMPARATOR);
	}

	/**
	 * 将Spring容器创建的拦截器注册到其所属上下文，并从手工注册集合中移除
	 * @author danyuan
	 */
	public static void registerInterceptor(ApplicationContext context, ApiMethodInterceptor interceptor){
		CONTEXT_REGISTRY.register(context, interceptor, interceptors, ORDER_COMPARATOR);
	}

	/**
	 * 上下文关闭时注销拦截器，避免静态注册表持续持有已销毁的上下文与实例
	 */
	public static void unregisterInterceptor(ApplicationContext context, ApiMethodInterceptor interceptor){
		CONTEXT_REGISTRY.unregister(context, interceptor);
	}

	/**
	 * 获取指定上下文已注册的拦截器快照；没有注册过时返回null
	 */
	static List<ApiMethodInterceptor> getContextInterceptors(ApplicationContext context){
		return CONTEXT_REGISTRY.get(context);
	}

	/**
	 * 解析当前请求应使用的拦截器链：
	 * 有Spring上下文时使用该上下文注册的实例，并合并业务手工注册的实例；
	 * 无上下文（手工new）时沿用静态注册集合，与改造前行为一致。
	 */
	private List<ApiMethodInterceptor> resolveInterceptors(){
		if(interceptors.isEmpty()){
			List<ApiMethodInterceptor> contextInterceptors = getContextInterceptors(applicationContext);
			if(contextInterceptors != null){
				return contextInterceptors;
			}
			return interceptors;
		}
		List<ApiMethodInterceptor> contextInterceptors = getContextInterceptors(applicationContext);
		if(contextInterceptors == null || contextInterceptors.isEmpty()){
			return interceptors;
		}
		List<ApiMethodInterceptor> merged = new ArrayList<>(interceptors);
		merged.addAll(contextInterceptors);
		merged.sort(ORDER_COMPARATOR);
		return merged;
	}
	
	static MethodMeta getMethodMeta(Method method){
		ConcurrentHashMap<Method, MethodMeta> cache = METHOD_META_CACHE.get(method.getDeclaringClass());
		MethodMeta meta = cache.get(method);
		if(meta != null){
			return meta;
		}
		meta = new MethodMeta(parameterNameDiscoverer.getParameterNames(method), method.getAnnotations(), method.getParameterAnnotations());
		if(meta.paramNames == null){
			//参数名解析失败时不缓存，避免把一次失败固化成永久结果；下次请求仍会重新解析
			return meta;
		}
		MethodMeta cached = cache.putIfAbsent(method, meta);
		return cached != null ? cached : meta;
	}
	
	/**
	 * @param invocation
	 * @return
	 * @throws Throwable
	 * @author danyuan
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if(!isApiPackageScopePresent() || AopUtils.isAopProxy(invocation.getThis())){
			return invocation.proceed();
		}
		
		if(log.isTraceEnabled()){
			log.trace("进入API接口调用切面!");
		}
		
		Class<?> targetClass = invocation.getThis().getClass();
		RequestContext.getContext().setTargetClass(targetClass);
		Object response = null;
		Object [] args = invocation.getArguments();
		Method method = invocation.getMethod();
		RequestContext.getContext().setMethod(method);
		MethodMeta methodMeta = getMethodMeta(method);
		String[] paramNames = methodMeta.paramNames;
	    //方法注解
	    Annotation[] methodAnnos = methodMeta.methodAnnos;
	    //参数注解，1维是参数，2维是注解
        Annotation[][] annotations = methodMeta.parameterAnnotations;
        //获取参数
		List<ApiRequestParameter> params = MethodInvokeUtil.parseParameters(paramNames, args, annotations, methodAnnos, easyApiConfig.getAllBasePackages());
	    
	    //获取接口信息
	    HttpServletRequest request = (HttpServletRequest)RequestContext.getContext().getRequest();
	    HandlerMethod handler = RequestContext.getContext().getHandler();
	    String controllerName = handler.getBeanType().getName();
	    controllerName = controllerName.substring(controllerName.lastIndexOf(".") + 1);
        String requestMethodName = handler.getMethod().getName();
        
        ApiInfo apiInfo = new ApiInfo();
	    apiInfo.setApiMethod(request.getMethod())
	    	.setApiName(String.format(API_NAME,controllerName, requestMethodName));
	    	
        String url = request.getRequestURI();
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if(statusCode != null && (statusCode != 200)){//是否是存在错误地址重定向URL
        	url = (String) request.getAttribute("javax.servlet.forward.servlet_path");
        }else{
        	//尝试从url中提取版本号
            String version = DEFAULT_API_VERSION;
            String str = url.substring(1);
            if(str.indexOf("/") > 0){
            	String first = str.substring(0, str.indexOf("/"));
            	if(PATTERN.matcher(first).matches()){
            		version = first;
            	}
            }
            apiInfo.setApiVersion(version);
        }	
        if(!StringUtils.isEmpty(request.getQueryString())){
        	url = url + "?" + request.getQueryString();
        }            
        apiInfo.setApiUri(url);
	    RequestContext.getContext().setApiInfo(apiInfo);
	    //业务方法是否已成功返回：成功返回后才允许将防重复提交码标记为已提交
	    boolean businessSucceeded = false;
	    try{
	    	//接口业务执行前拦截处理
		    try{
		    	for(ApiMethodInterceptor interceptor : resolveInterceptors()){
		    		interceptor.preInvoke(params, method, targetClass);
			    }	
		    }finally{	    	
		    	apiLogInterceptor.preInvoke(params, method, targetClass);
		    }		    
		    apiInfo.setReqParams(params);
		    //执行业务方法
		    response = invocation.proceed();
		    businessSucceeded = true;
		    //接口执行完成后拦截处理
		    for(ApiMethodInterceptor interceptor : resolveInterceptors()){
		    	response = interceptor.afterInvoke(params, response, method, targetClass);
		    }	
		    apiInfo.setResponse(response);	
	    }finally{
	    	//业务成功或失败都需要通知防重复提交拦截器；失败时传入失败标记，
	    	//由防重复提交拦截器根据配置决定释放提交码还是保持历史行为
	    	noRepeatSubmitInterceptor.afterInvoke(params, businessSucceeded ? response : INVOKE_FAILED_MARKER, method, targetClass);
	    }		
		return response;
	}	
	
	// 判断目标类是否为指定包名下的
	private boolean isApiPackageScopePresent() {
		HandlerMethod handlerMethod = RequestContext.getContext().getHandler();
		if(handlerMethod != null){
			Method method = handlerMethod.getMethod();
			Set<String> basePackages = easyApiConfig.getAllBasePackages();
			InterceptCache interceptCache = INTERCEPT_CACHE.get(method.getDeclaringClass());
			//根包集合内容变化时（例如运行期修改了easyapi.basePackages），自动失效缓存
			interceptCache.ensurePackages(basePackages);
			Boolean cached = interceptCache.results.get(method);
			if(cached != null){
				return cached;
			}
			MethodParameter returnParam = handlerMethod.getReturnType();
			Class<?> clazz = method.getDeclaringClass();
			Class<?> returnClazz = returnParam.getParameterType();
			boolean needIntercept = ClassOriginCheckUtil.isNeedIntercept(clazz, returnClazz, method, basePackages);
			interceptCache.results.putIfAbsent(method, needIntercept);
			return needIntercept;
		}		
		return false;
	}	

}
