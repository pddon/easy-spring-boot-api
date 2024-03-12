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
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.pddon.framework.easyapi.interceptor.impl.ApiLogInterceptor;
import com.pddon.framework.easyapi.interceptor.impl.NoRepeatSubmitInterceptor;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import lombok.extern.slf4j.Slf4j;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import com.pddon.framework.easyapi.aspect.ApiExceptionAspector;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiInfo;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.utils.ClassOriginCheckUtil;
import com.pddon.framework.easyapi.utils.MethdInvokeUtil;

@Component
@Slf4j
public class ApiInvokeMethodInterceptorManager implements MethodInterceptor {

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
	
	private static List<ApiMethodInterceptor> interceptors = new ArrayList<>();
	
	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
	/**
	 * 添加接口拦截器
	 * @author danyuan
	 */
	public static void addInterceptor(ApiMethodInterceptor interceptor){
		interceptors.add(interceptor);
		//添加成功后重新进行排序,使拦截器按特定顺序进行拦截处理
		interceptors.sort(new Comparator<ApiMethodInterceptor>() {

			@Override
			public int compare(ApiMethodInterceptor o1, ApiMethodInterceptor o2) {
				if(o1.order() > o2.order()){
					return 1;
				}else if(o1.order() < o2.order()){
					return -1;
				}else{
					return 0;
				}
			}
		});
	}
	
	/**
	 * @param invocation
	 * @return
	 * @throws Throwable
	 * @author danyuan
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if(!isApiPackageScopePresent()){
			return invocation.proceed();
		}
		
		if(log.isTraceEnabled()){
			log.trace("进入API接口调用切面!");
		}
		
		Class<?> targetClass = invocation.getThis().getClass();	
		RequestContext.getContext().setTargetClass(targetClass);
		Object response = null;
		Object [] args=invocation.getArguments();		
		Method method = invocation.getMethod();
		RequestContext.getContext().setMethod(method);
		String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
	    //方法注解
	    Annotation[] methodAnnos = method.getAnnotations();
	    //参数注解，1维是参数，2维是注解
        Annotation[][] annotations = method.getParameterAnnotations();
        //获取参数
		List<ApiRequestParameter> params = MethdInvokeUtil.parseParameters(paramNames, args, annotations, methodAnnos);
	    
	    //获取接口信息
	    HttpServletRequest request = RequestContext.getContext().getRequest();
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
	    try{
	    	//接口业务执行前拦截处理
		    try{
		    	for(ApiMethodInterceptor interceptor : interceptors){
		    		interceptor.preInvoke(params, method, targetClass);
			    }	
		    }finally{	    	
		    	apiLogInterceptor.preInvoke(params, method, targetClass);
		    }		    
		    apiInfo.setReqParams(params);
		    //执行业务方法
		    response = invocation.proceed();
		    //接口执行完成后拦截处理
		    for(ApiMethodInterceptor interceptor : interceptors){
		    	response = interceptor.afterInvoke(params, response, method, targetClass);
		    }	
		    apiInfo.setResponse(response);	
	    }finally{
	    	//释放防重复提交码
	    	noRepeatSubmitInterceptor.afterInvoke(params, response, method, targetClass);
	    }		
		return response;
	}	
	
	// 判断目标类是否为指定包名下的
	private boolean isApiPackageScopePresent() {
		HandlerMethod handlerMethod = RequestContext.getContext().getHandler();
		if(handlerMethod != null){
			MethodParameter returnParam = handlerMethod.getReturnType();
			Class<?> clazz = handlerMethod.getMethod().getDeclaringClass();
			Class<?> returnClazz = returnParam.getParameterType();
			if(ClassOriginCheckUtil.isNeedIntercept(clazz, returnClazz, handlerMethod.getMethod(), easyApiConfig.getAllBasePackages())){
				return true;
			}
		}		
		return false;
	}	

}
