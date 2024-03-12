/**  
* Title DefaultExceptionHandler.java  
* Description  
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.exception.handler.impl;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.exception.handler.CommonExceptionHandler;
import com.pddon.framework.easyapi.utils.IOUtils;


@Service
@Slf4j
public class DefaultExceptionHandler implements CommonExceptionHandler{

	@Autowired
	private LanguageTranslateManager languageTranslateManager;
	
	/**
	 * @author danyuan
	 */
	@Override
	public boolean support(Exception e) {
		return true;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public DefaultResponseWrapper<?> handle(HttpServletRequest request,
                                            HttpServletResponse response, Exception e) {
		 String locale = RequestContext.getContext().getLocale();
		 String errorMsg = null;
		 Integer errorCode = null;
		if (e instanceof NoHandlerFoundException) {
			//接口不存在
			errorMsg = languageTranslateManager.get(
					ErrorCodes.NOT_FOUND.getMsgCode(), locale
					);			
			errorCode = ErrorCodes.NOT_FOUND.getCode();
		} else if (e instanceof HttpMessageNotReadableException) {
			//请求json数据异常
			errorMsg = languageTranslateManager.get(
					ErrorCodes.PARAM_FORMAT_ERROR.getMsgCode(), locale,e.getMessage()
					);			
			errorCode = ErrorCodes.PARAM_FORMAT_ERROR.getCode();
		}else if(e instanceof HttpRequestMethodNotSupportedException){
			//HTTP Method不合法
			errorMsg = languageTranslateManager.get(
					ErrorCodes.INVALID_HTTP_ACTION.getMsgCode(), locale,
					request.getRequestURI(),
					request.getMethod().toString()
					);			
			errorCode = ErrorCodes.INVALID_HTTP_ACTION.getCode();
		} else if(e instanceof HttpMediaTypeNotSupportedException){
			//接口上传数据类型错误			
			errorMsg = languageTranslateManager.get(
					ErrorCodes.INVALID_MEDIA_TYPE.getMsgCode(), locale,
					((HttpMediaTypeNotSupportedException)e).getContentType().toString()
					);			
			errorCode =ErrorCodes.INVALID_MEDIA_TYPE.getCode();
		} else if (e instanceof AsyncRequestTimeoutException || e instanceof TimeoutException) {
			//接口超时
			errorMsg = languageTranslateManager.get(
					ErrorCodes.TIMEOUT.getMsgCode(), locale);			
			errorCode = ErrorCodes.TIMEOUT.getCode();
		}else if(e instanceof IOException){//NETWORK_ERROR
			errorMsg = languageTranslateManager.get(
					ErrorCodes.NETWORK_ERROR.getMsgCode(), locale,e.getMessage());
			errorCode = ErrorCodes.NETWORK_ERROR.getCode();
		}else{
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			errorMsg = languageTranslateManager.get(
					ErrorCodes.UNKOWN_ERROR.getMsgCode(), locale, e.getMessage());
			errorCode = ErrorCodes.UNKOWN_ERROR.getCode();
			
		}
		log.error(e.getMessage());
		if(log.isDebugEnabled() || log.isTraceEnabled()){
			log.error(IOUtils.getThrowableInfo(e));
		}else{
			log.error(IOUtils.getThrowableInfoLess(e));
		}
		return new DefaultResponseWrapper<>(errorCode, errorMsg);
	}

}
