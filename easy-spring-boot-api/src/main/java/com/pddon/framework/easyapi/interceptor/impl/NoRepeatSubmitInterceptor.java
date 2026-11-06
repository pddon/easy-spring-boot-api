/**  
* Title NoRepeatSubmitInterceptor.java  
* Description  检查接口防重复提交拦截器
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

import com.pddon.framework.easyapi.CheckRepeatManager;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.interceptor.AbstractApiMethodInterceptor;
import com.pddon.framework.easyapi.interceptor.ApiInvokeMethodInterceptorManager;
import com.pddon.framework.easyapi.properties.EasyApiConfig;

@Service
@Slf4j
public class NoRepeatSubmitInterceptor extends AbstractApiMethodInterceptor {

	@Autowired
	private CheckRepeatManager checkRepeatManager;

	@Autowired
	private EasyApiConfig easyApiConfig;

	/**
	 * 当前线程是否已成功获取防重复提交码。
	 * 只有成功获取码的请求才有权在失败时释放它，避免因校验被拒绝的并发请求
	 * 误释放他人正在处理中的码；ThreadLocal必须在afterInvoke的finally中清理。
	 */
	private static final ThreadLocal<Boolean> TOKEN_ACQUIRED = new ThreadLocal<>();
	
	/**
	 * @author danyuan
	 */
	@Override
	public void preInvoke(List<ApiRequestParameter> requestParams, Method method,
			Class<?> targetClass) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("开始校验接口防重复提交！");
		}
		if(RequestContext.getContext().getApiRestrictions().getUniqueSubmit()){
			//检查接口防重复提交
			checkRepeatManager.check(RequestContext.getContext().getRepeatCode());
			//记录本次请求已持有提交码，失败时只有持有者才能释放
			TOKEN_ACQUIRED.set(Boolean.TRUE);
		}
		
	}

	/**
	 * @author danyuan
	 */
	@Override
	public Object afterInvoke(List<ApiRequestParameter> requestParams, Object resp,
			Method method, Class<?> targetClass) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("释放接口防重复提交码！");
		}
		if(RequestContext.getContext().getApiRestrictions().getUniqueSubmit()){
			String repeatCode = RequestContext.getContext().getRepeatCode();
			try{
				if(ApiInvokeMethodInterceptorManager.isInvokeFailedMarker(resp)){
					if(easyApiConfig.getNoRepeatSubmit().isReleaseOnFailure()){
						//业务失败：仅当本次请求确实持有提交码时才释放，允许纠正后立即重试
						if(Boolean.TRUE.equals(TOKEN_ACQUIRED.get())){
							checkRepeatManager.release(repeatCode);
							if(log.isTraceEnabled()){
								log.trace("业务执行失败，释放接口防重复提交码:[{}]", repeatCode);
							}
						}
					}else{
						//兼容旧行为：失败同样标记为已提交
						checkRepeatManager.used(repeatCode);
					}
				}else{
					//标记接口防重复提交成功
					checkRepeatManager.used(repeatCode);
				}
			}finally{
				TOKEN_ACQUIRED.remove();
			}
		}
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
