/**  
* Title ApiLogInterceptor.java  
* Description  会话信息校验拦截器
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor.impl;

import java.lang.reflect.Method;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pddon.framework.easyapi.SessionManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.dto.Session;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.interceptor.AbstractApiMethodInterceptor;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;

@Service
@Setter
@Slf4j
public class SessionInterceptor extends AbstractApiMethodInterceptor {
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	private SystemParameterRenameProperties systemParameterProperties;

	/**
	 * @author danyuan
	 */
	@Override
	public void preInvoke(List<ApiRequestParameter> requestParams, Method method,
			Class<?> targetClass) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("开始校验接口会话信息！");
		}
		if(RequestContext.getContext().getApiRestrictions().getSession()){
			//检查会话信息
			String sessionId = RequestContext.getContext().getSessionId();
			Session session = sessionManager.get(sessionId);
			if(session == null){
				throw new BusinessException(ErrorCodes.INVALID_SESSION_ID).setParam(systemParameterProperties.getSessionId(), sessionId);
			}			
			RequestContext.getContext().setSession(session);
		}
	}

	/**
	 * @author danyuan
	 */
	@Override
	public Object afterInvoke(List<ApiRequestParameter> requestParams, Object resp,
			Method method, Class<?> targetClass) throws Throwable {
		return resp;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public int order() {
		return 0;
	}

}
