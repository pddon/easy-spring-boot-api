/**  
* Title ApiDataFormatInterceptor.java  
* Description  接口请求、响应数据加工处理拦截器
* @author danyuan
* @date Nov 15, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.dataformat.FieldDataFormatHandler;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.interceptor.AbstractApiMethodInterceptor;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.IOUtils;

@Service
@Slf4j
public class ApiDataFormatInterceptor extends AbstractApiMethodInterceptor {

	private static List<FieldDataFormatHandler> handlers = new ArrayList<>();
	
	@Autowired
	private LanguageTranslateManager languageTranslateManager;
	
	public static void addDataFormatHandler(FieldDataFormatHandler handler){
		handlers.add(handler);
		//添加成功后重新进行排序,使拦截器按特定顺序进行拦截处理
		handlers.sort(new Comparator<FieldDataFormatHandler>() {

			@Override
			public int compare(FieldDataFormatHandler o1, FieldDataFormatHandler o2) {
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
	 * @author danyuan
	 */
	@Override
	public int order() {
		return -2;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void preInvoke(List<ApiRequestParameter> requestParams,
			Method method, Class<?> targetClass) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("开始对请求内容进行密文解密！");
		}
		if(requestParams != null && requestParams.size() > 0){
			for(ApiRequestParameter parameter : requestParams){//每个参数的每个子字段都递归检查一遍
				Object obj = parameter.getParam();
				Map<Class<?>, Annotation> annos = new HashMap<>();
				if(parameter.getAnnotations() != null){
					for(Annotation anno : parameter.getAnnotations()){
						annos.put(anno.annotationType(), anno);
					}
				}					
				obj = BeanPropertyUtil.decorateObj(parameter.getParamName(), obj, annos,  (fieldName, data, annotations)->{
					for(FieldDataFormatHandler handler : handlers){
						if(handler.handleRequest()){
							try{
								data = handler.handle(fieldName, data, annotations);
							}catch(BusinessException e){
								log.warn(languageTranslateManager.get(e.getMessage(), e.getParams()));
							}catch(Exception e){
								log.warn(IOUtils.getThrowableInfo(e));
							}							
						}						
					}						
					return data;
				});
				parameter.getArgs()[parameter.getIndex()] = obj;//改变参数值
				parameter.setParam(obj);
			}
		}	
	}

	/**
	 * @author danyuan
	 */
	@Override
	public Object afterInvoke(List<ApiRequestParameter> requestParams,
			Object resp, Method method, Class<?> targetClass) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("开始对响应内容进行明文加密！");
		}
		Map<Class<?>, Annotation> annos = new HashMap<>();
		if(method.getAnnotations() != null){
			for(Annotation anno : method.getAnnotations()){
				annos.put(anno.annotationType(), anno);
			}
		}					
		resp = BeanPropertyUtil.decorateObj("resp", resp, annos,  (fieldName, data,annotations)->{
			for(FieldDataFormatHandler handler : handlers){
				if(handler.handleResponse()){
					try{
						data = handler.handle(fieldName, data, annotations);
					}catch(BusinessException e){
						log.warn(languageTranslateManager.get(e.getMessage(), e.getParams()));
					}catch(Exception e){
						log.warn(IOUtils.getThrowableInfo(e));
					}	
				}				
			}				
			return data;
		});
		return resp;
	}

}
