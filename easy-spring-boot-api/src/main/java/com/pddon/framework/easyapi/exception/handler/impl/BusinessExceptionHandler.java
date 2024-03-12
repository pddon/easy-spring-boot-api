/**  
* Title BusinessExceptionHandler.java  
* Description  业务异常处理器
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.exception.handler.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.exception.handler.AbstractCommonExceptionHandler;

@Service
@Slf4j
public class BusinessExceptionHandler extends AbstractCommonExceptionHandler {

	@Autowired
	private LanguageTranslateManager languageTranslateManager;
	
	/**
	 * @author danyuan
	 */
	@Override
	public boolean support(Exception e) {
		if (e instanceof BusinessException || (e.getCause() instanceof BusinessException)){
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
		BusinessException be = null;
		if(e instanceof BusinessException){
			be = (BusinessException) e;
		}else{
			be = (BusinessException) e.getCause();
		}
		String locale = RequestContext.getContext().getLocale();
		String errorMsg = null;
		if(StringUtils.isEmpty(be.getSubCode())){
			//系统错误码
			errorMsg = languageTranslateManager.get(
					ErrorCodes.getByCode(be.getCode()).getMsgCode(), locale,
					be.getParams());
		}else{			
			errorMsg = languageTranslateManager.get(
					be.getSubCode(), locale,
					be.getParams());		
		}
		be.setMsg(errorMsg);
		if(log.isTraceEnabled()){
			log.trace("subError:{},exception:{},locale:{}",be.getSubCode(), be.getMsg(),locale);
		}
		return new DefaultResponseWrapper<>(be);
	}

}
