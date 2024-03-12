/**  
* Title InvokeTimesInterceptor.java  
* Description  打印接口调用频率控制拦截器
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor.impl;

import java.lang.reflect.Method;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pddon.framework.easyapi.InvokeTimesManager;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiInfo;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.interceptor.AbstractApiMethodInterceptor;

@Service
@Slf4j
public class InvokeTimesInterceptor extends AbstractApiMethodInterceptor {

	@Autowired
	private InvokeTimesManager invokeTimesManager;
	
	/**
	 * @author danyuan
	 */
	@Override
	public void preInvoke(List<ApiRequestParameter> requestParams, Method method,
			Class<?> targetClass) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("开始检查接口调用频次是否合法！");
		}
		ApiInfo apiInfo = RequestContext.getContext().getApiInfo();
		//校验某客户端IP对所有接口的总调用次数是否超限
		invokeTimesManager.checkClientIpInvokeTimes(RequestContext.getContext().getClientIp());
		//校验应用的客户端对所有接口的总调用次数是否超限
		invokeTimesManager.checkAppTotalInvokeTimes(RequestContext.getContext().getAppId());
		//校验应用的客户端对所有接口的总调用次数是否超限
		invokeTimesManager.checkAppUserTotalInvokeTimes(RequestContext.getContext().getAppId(), RequestContext.getContext().getUserId());
		//校验应用的客户端对某接口的总调用次数是否超限
		invokeTimesManager.checkAppInvokeTimes(RequestContext.getContext().getAppId(), apiInfo.getApiName());
		//校验应用内某用户对某接口的总调用次数是否超限
		invokeTimesManager.checkAppUserInvokeTimes(RequestContext.getContext().getAppId(), 
				RequestContext.getContext().getUserId(), apiInfo.getApiName());
		//校验应用内某用户某次会话内对所有接口的调用次数是否超限
		invokeTimesManager.checkAppUserSessionTotalInvokeTimes(RequestContext.getContext().getAppId(), 
				RequestContext.getContext().getSessionId());
		//校验应用内某用户某次会话内对某接口的调用次数是否超限
		invokeTimesManager.checkAppUserSessionInvokeTimes(RequestContext.getContext().getAppId(), 
				RequestContext.getContext().getSessionId(), apiInfo.getApiName());
		//校验应用当前一段时间窗口内对某接口的调用次数是否超限
		invokeTimesManager.checkAppTimeSectionInvokeTimes(RequestContext.getContext().getAppId(), apiInfo.getApiName());
		//校验应用内某用户当前一段时间窗口内对某接口的调用次数是否超限
		invokeTimesManager.checkAppUserTimeSectionInvokeTimes(RequestContext.getContext().getAppId(), 
				RequestContext.getContext().getUserId(), apiInfo.getApiName());
		//校验某客户端IP最近一段时间内对所有接口的总调用次数是否超限
		invokeTimesManager.checkClientIpTimeSectionInvokeTimes(RequestContext.getContext().getClientIp());
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
		return -1;
	}

}
