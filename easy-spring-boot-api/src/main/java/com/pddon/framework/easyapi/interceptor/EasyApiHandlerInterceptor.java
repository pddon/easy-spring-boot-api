/**  
* Title SignHandlerInterceptor.java  
* Description  
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.utils.ClassOriginCheckUtil;
import com.pddon.framework.easyapi.utils.IOUtils;
import com.pddon.framework.easyapi.utils.request.BodyReaderHttpServletRequestWrapper;
import com.pddon.framework.easyapi.utils.request.HttpHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Scope("singleton")
@Slf4j
public final class EasyApiHandlerInterceptor implements HandlerInterceptor {
	
	@Autowired
	private EasyApiConfig easyApiConfig;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		if(log.isTraceEnabled()){
			log.trace("进入接口系统参数解析拦截器!");
		}
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			Class<?> clazz = handlerMethod.getMethod().getDeclaringClass();
			RequestContext.getContext().setHandler(handlerMethod);
			RequestContext.getContext().setRequest(request);
			RequestContext.getContext().setResponse(response);
			if(ClassOriginCheckUtil.isBasePackagesChild(clazz, easyApiConfig.getAllBasePackages())){
				//只解析指定包名下系统公共参数,不区分资源类型
				if(log.isTraceEnabled()){
					log.trace("开始解析系统参数!");
				}
				parseSystemParams(request);
				if(log.isTraceEnabled()){
					log.trace("解析系统参数完成!");
				}
				String path=request.getServletPath();
				if(path.replaceFirst("/", "").trim().equals("error")){//处理默认的错误结果页
					//throw new ApiBusinessException(ErrorCodes.INVALID_METHOD).setParam(request.getRequestURI());
				}
			}
			//设置日志中的requestId
			String requestId = request.getHeader("X-Trace-Id");
			if(StringUtils.isEmpty(requestId)){
				requestId = request.getHeader("X-Request-Id");
			}
			if(!StringUtils.isEmpty(requestId)){
				String key = SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.REQUEST_ID);
				RequestContext.getContext().setAttachment(key, requestId);
			}else{
				requestId = RequestContext.getContext().getRequestId();
			}
			MDC.put("requestId", requestId);
		}
		
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}
	
	private void parseSystemParams(HttpServletRequest request){
		// 请求方法
        String method = request.getMethod();
        HttpServletRequest requestWrapper = request;
        //先通过URL获取参数
        SystemParameterRenameProperties.DEFAULT_PARAM_MAP.keySet().stream().forEach(key -> {
        	String paramName = SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(key);
        	String paramValue = request.getParameter(paramName);
        	if(!StringUtils.isEmpty(paramValue)){
        		RequestContext.getContext().setAttachment(paramName, paramValue);
        	}        	
        });
		if(request.getContentType() != null && request.getContentType().contains("multipart")){
			return;
		}
        //再通过请求体获取系统参数
        if(RequestMethod.POST.name().equals(method) || RequestMethod.PUT.name().equals(method)){
        	String body = null;
        	//判断action
        	String action=request.getContentType();
        	if(action!=null && action.contains(MediaType.APPLICATION_JSON_VALUE)){
        		try {
        			 if(!(request instanceof BodyReaderHttpServletRequestWrapper)){
        				 requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
        			 }
                    
                    body = HttpHelper.getBodyString(requestWrapper);
                    if (!StringUtils.isEmpty(body)) {
                    	if(log.isTraceEnabled()){
							String intro = body.length() > 1024 ? body.substring(0, 1024) : body;
                        	log.trace("request body:{}", intro);
                        }
                    	try{
                        	// 解析json                
                        	//ObjectMapper mapper = new ObjectMapper();
							//objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
                        	JsonNode json = objectMapper.readTree(body);
                        	SystemParameterRenameProperties.DEFAULT_PARAM_MAP.keySet().stream().forEach(key -> {
                            	String paramName = SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(key);
                            	if(json.has(paramName)){
                            		String paramValue = json.get(paramName).asText();
                                	if(!StringUtils.isEmpty(paramValue)){
                                		RequestContext.getContext().setAttachment(paramName, paramValue);
                                	} 
                            	}                            	       	
                            });
                        }catch(Exception e){
                        	log.warn(IOUtils.getThrowableInfo(e));
                        }  
                    }
				} catch (IOException e) {
					if(log.isDebugEnabled()){
						log.error(e.getMessage());
						log.error(IOUtils.getThrowableInfo(e));
					}					
				}   
        	}               
        }
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		response.setHeader("Access-Control-Expose-Headers","X-Request-Id");
		//增加链路追踪ID返回
		response.addHeader("X-Request-Id", RequestContext.getContext().getRequestId());
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}
	
}
