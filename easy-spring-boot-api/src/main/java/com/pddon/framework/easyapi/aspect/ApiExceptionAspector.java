/**  
 * Title ApiExceptionAspector.java  
 * Description  全局异常监听器
 * @author danyuan
 * @date Nov 6, 2018
 * @version 1.0.0
 * blog pddon.cn
 */
package com.pddon.framework.easyapi.aspect;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.exception.handler.CommonExceptionHandler;
import com.pddon.framework.easyapi.exception.handler.impl.DefaultExceptionHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.pddon.framework.easyapi.utils.IOUtils;



@RestControllerAdvice
@ControllerAdvice
@Service
@Data
@Slf4j
public class ApiExceptionAspector implements HandlerExceptionResolver{

	private static Set<CommonExceptionHandler> handlers = new HashSet<>();
	
	@Autowired
	private DefaultExceptionHandler defaultExceptionHandler;

	/**
	 * @author danyuan
	 */
	public static void addHandler(CommonExceptionHandler handler) {
		handlers.add(handler);
	}

	@ExceptionHandler
	public DefaultResponseWrapper<?> resolveRestControllerException(HttpServletRequest request,
                                                                    HttpServletResponse response, Object handler, Exception e) throws ClassNotFoundException {
		
		response.setStatus(HttpServletResponse.SC_OK);
		Exception ex = e;
		if(e instanceof UndeclaredThrowableException){
			Throwable te = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
			if(te instanceof Exception){
				ex = (Exception) te;
			}
		}
		DefaultResponseWrapper<?> responseWrapper = null;
		
		for(CommonExceptionHandler h : handlers){
			if(h.support(ex)){
				responseWrapper = h.handle(request, response, ex);
				break;
			}
		}
		//如果没有业务异常处理器处理异常，则使用默认异常处理器进行处理
		if(responseWrapper == null){
			responseWrapper = defaultExceptionHandler.handle(request, response, ex);
		}	
		
		return responseWrapper;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {
		Object resp = null;
		try {
			resp = this.resolveRestControllerException(request, response, handler, ex);
		} catch (ClassNotFoundException e) {
			log.error(IOUtils.getThrowableInfo(e));	
			resp = defaultExceptionHandler.handle(request, response, e);
		}
		ModelAndView data=new ModelAndView();
		data.addObject(resp);
		data.setView(new MappingJackson2JsonView());
		return data;
	}

}
