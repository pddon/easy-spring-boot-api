/**  
* Title InvalidateExceptionHandler.java  
* Description  参数校验失败异常处理器
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.exception.handler.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.controller.response.ErrorInfo;
import com.pddon.framework.easyapi.controller.response.ErrorResponse;
import com.pddon.framework.easyapi.exception.handler.CommonExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;

@Service
public class InvalidationExceptionHandler implements CommonExceptionHandler {

	@Autowired
	private LanguageTranslateManager languageTranslateManager;
	
	/**
	 * @author danyuan
	 */
	@Override
	public boolean support(Exception e) {
		if (e instanceof BindException 
				|| e instanceof MethodArgumentNotValidException 
				|| e instanceof ConstraintViolationException 
				|| e instanceof MissingServletRequestParameterException){
			return true;
		}
		return false;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public DefaultResponseWrapper<?> handle(HttpServletRequest request,
                                            HttpServletResponse response, Exception e) {
		String systemError = null;			
		String locale = RequestContext.getContext().getLocale();
		List<FieldError> errors = null;
		Set<ConstraintViolation<?>> constraints = null;
		if(e instanceof BindException){
			BindException ex=(BindException)e;
			errors = ex.getFieldErrors();
		}else if(e instanceof MethodArgumentNotValidException) {
			MethodArgumentNotValidException ex = (MethodArgumentNotValidException)e;
			errors = ex.getBindingResult().getFieldErrors();
		}else if(e instanceof ConstraintViolationException){
			ConstraintViolationException ex = (ConstraintViolationException) e;
			constraints = ex.getConstraintViolations();
		}else if ( e instanceof MissingServletRequestParameterException){
			MissingServletRequestParameterException ex = (MissingServletRequestParameterException) e;
			systemError=languageTranslateManager.get(
					ErrorCodes.PARAM_LOST.getMsgCode(), locale,ex.getParameterName()
					);
			return new DefaultResponseWrapper<>(
					ErrorCodes.PARAM_LOST.getCode(), systemError);
		}
		ErrorResponse errorResp=null;
		systemError=languageTranslateManager.get(
				ErrorCodes.INVALID_PARAM.getMsgCode(), locale,"",""
				);
		errorResp=new ErrorResponse().setSubErrors(new ArrayList<ErrorInfo>());
		if(errors != null){
			for(FieldError error :errors){
				String filed=error.getField();
				String errorType=error.getCode();
				Character first=errorType.charAt(0);
				errorType="system."+errorType.replaceFirst(String.valueOf(first), first.toString().toLowerCase());
				String code=languageTranslateManager.get(errorType,locale);
				if(code.equals(errorType)){//默认为参数非法
					code=error.getDefaultMessage();
				}
				String subErrMsg=languageTranslateManager.get(
						ErrorCodes.INVALID_PARAM.getMsgCode(), locale,filed,code
						);
				ErrorInfo info=new ErrorInfo();
				info.setError(errorType)
					.setMsg(subErrMsg);
				errorResp.getSubErrors().add(info);
			}
		}else if(constraints != null){
			for(ConstraintViolation<?> constraint :constraints){	
				ErrorInfo info=new ErrorInfo();
				info.setError(constraint.getMessageTemplate().substring(1, constraint.getMessageTemplate().length()-1))
					.setMsg("[" + constraint.getPropertyPath().toString() + "]" + constraint.getMessage());
				errorResp.getSubErrors().add(info);
			}
		}		
		DefaultResponseWrapper<ErrorResponse> responseWrapper = null;
		if(errorResp.getSubErrors().size() == 1){
			//单个错误直接返回
			systemError= errorResp.getSubErrors().get(0).getMsg();
			responseWrapper = new DefaultResponseWrapper<>(
					ErrorCodes.INVALID_PARAM.getCode(), systemError);
		}else{
			responseWrapper = new DefaultResponseWrapper<>(
					ErrorCodes.INVALID_PARAM.getCode(), systemError, errorResp);
		}	

		return responseWrapper;
	}

}
