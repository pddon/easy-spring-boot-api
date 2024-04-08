/**  
 * Title ApiInvokeAspector.java  
 * Description  
 * @author danyuan
 * @date Jun 12, 2019
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.pddon.framework.easyapi.annotation.Decrypt;
import com.pddon.framework.easyapi.annotation.IgnoreSign;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.interceptor.ApiMethodInterceptor;
import com.pddon.framework.easyapi.interceptor.impl.ApiLogInterceptor;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

import com.pddon.framework.easyapi.dto.ApiInfo;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.ClassOriginCheckUtil;

//@Aspect
//@Service
@Slf4j
@Deprecated
public class ApiInvokeAspector {
	
	private static String DEFAULT_API_VERSION = "1.0";
	private static String API_NAME = "%s::%s";
	private static Pattern PATTERN = Pattern.compile("[0-9\\.]*");
	
	@Autowired(required = false)
	private EasyApiConfig config;
	
	@Autowired
	private ApiLogInterceptor apiLogInterceptor;
	
	@Autowired
	private ApiExceptionAspector apiExceptionAspector;
	
	private static List<ApiMethodInterceptor> interceptors = new ArrayList<>();
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
	
	@Around( value = "@annotation(org.springframework.web.bind.annotation.PostMapping)" )
	public Object postAspect(ProceedingJoinPoint joinPoint ) throws Throwable {
		return doService(joinPoint);		
	}
	
	@Around( value = "@annotation(org.springframework.web.bind.annotation.GetMapping)" )
	public Object getAspect(ProceedingJoinPoint joinPoint ) throws Throwable {
		return doService(joinPoint);		
	}
	
	@Around( value = "@annotation(org.springframework.web.bind.annotation.RequestMapping)" )
	public Object requestAspect(ProceedingJoinPoint joinPoint ) throws Throwable {
		return doService(joinPoint);		
	}
	
	@Around( value = "@annotation(org.springframework.web.bind.annotation.PutMapping)" )
	public Object putAspect(ProceedingJoinPoint joinPoint ) throws Throwable {
		return doService(joinPoint);		
	}
	
	@Around( value = "@annotation(org.springframework.web.bind.annotation.DeleteMapping)" )
	public Object deleteAspect(ProceedingJoinPoint joinPoint ) throws Throwable {
		return doService(joinPoint);		
	}
	
	@Around( value = "@annotation(org.springframework.web.bind.annotation.PatchMapping)" )
	public Object patchAspect(ProceedingJoinPoint joinPoint ) throws Throwable {
		return doService(joinPoint);		
	}
	
	private Object doService(ProceedingJoinPoint joinPoint) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("进入API接口调用切面!");
		}
		Object resp = null;
		Class<?> targetClass = joinPoint.getTarget().getClass();
		RequestContext.getContext().setTargetClass(targetClass);
		if(ClassOriginCheckUtil.isBasePackagesChild(targetClass, config.getAllBasePackages())){
			//只拦截应用自己包路径下的api
			
			Object [] args=joinPoint.getArgs();			
			MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
			String[] parameterNames = methodSignature.getParameterNames();
		    Method method = methodSignature.getMethod();
		    //方法注解
		    Annotation[] methodAnnos = method.getAnnotations();
		    //参数注解，1维是参数，2维是注解
	        Annotation[][] annotations = method.getParameterAnnotations();
	        //获取参数
			List<ApiRequestParameter> params = parseParameters(parameterNames, args, annotations, methodAnnos);
		    
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
		    resp = joinPoint.proceed(args);
		    //接口执行完成后拦截处理
		    for(ApiMethodInterceptor interceptor : interceptors){
		    	resp = interceptor.afterInvoke(params, resp, method, targetClass);
		    }	
		    apiInfo.setResponse(resp);
		}else{
			//执行业务方法
		    resp = joinPoint.proceed();
		}	
		return resp;
	}
	
	private List<ApiRequestParameter> parseParameters(String[] parameterNames, Object [] args, 
			Annotation[][] annotations, Annotation[] methodAnnos){
		List<ApiRequestParameter> parameters = new ArrayList<>();
		//提取业务请求参数
		ApiRequestParameter param = null;
	    for(int i=0; i<args.length; i++){
	    	if(args[i] != null && (BeanPropertyUtil.isBaseType(args[i]) 
	    			|| ClassOriginCheckUtil.isBasePackagesChild(args[i].getClass(), config.getAllBasePackages()))){
	    		param = new ApiRequestParameter();
	    		if(BeanPropertyUtil.isBaseType(args[i])){
	    			String paramName = getBaseTypeParamName(annotations[i]);
	    			if(StringUtils.isEmpty(paramName)){
	    				param.setParamName(parameterNames[i]);
	    			}else{
	    				param.setParamName(paramName);
	    			}    			
	    		}else{
	    			param.setParamName(parameterNames[i]);
	    		}	    		
	    		if(checkIsAnno(annotations[i], IgnoreSign.class)){
	    			param.setNeedSign(false);
	    		}else{
	    			param.setNeedSign(true);
	    		}
	    		if(checkIsAnno(annotations[i], Decrypt.class)){
	    			param.setDecrpt(true);
	    		}else{
	    			param.setDecrpt(false);
	    		}
	    		param.setParam(args[i]);
	    		
  		
	    		Annotation[] allAnnos = null;
	    		if(methodAnnos != null && (annotations[i] != null)){
	    			allAnnos = Arrays.copyOf(methodAnnos, methodAnnos.length + annotations[i].length);//数组扩容
	    			System.arraycopy(annotations[i], 0, allAnnos, methodAnnos.length, annotations[i].length);
	    		}else if(methodAnnos != null){
	    			allAnnos = methodAnnos;
	    		}else if(annotations[i] != null){
	    			allAnnos = annotations[i];
	    		}	    		
	    		param.setAnnotations(allAnnos);
	    		param.setIndex(i);
	    		param.setArgs(args);
	    		parameters.add(param);
	    	}
	    }		
		return parameters;
	}
	
	private boolean checkIsAnno(Annotation[] annotations, Class<?> annoClass){
		for(Annotation anno : annotations){
			if(anno.annotationType().equals(annoClass)){
				return true;
			}
		}
		return false;
	}
	
	private String getBaseTypeParamName(Annotation[] annotations){
		for(Annotation anno : annotations){
			if(anno.annotationType().equals(RequestParam.class)){
				RequestParam reqAnno = (RequestParam) anno;
				return reqAnno.value();				
			}
		}
		return null;
	}
}
