/**  
* Title ApiLogInterceptor.java  
* Description  自动校验请求参数拦截器
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.interceptor.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.interceptor.AbstractApiMethodInterceptor;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;

@Service
@Slf4j
public class ValidateParamsInterceptor extends AbstractApiMethodInterceptor {

	private Validator validator;
	
	/**
	 * @author danyuan
	 */
	public ValidateParamsInterceptor() {
		this.validator=Validation.buildDefaultValidatorFactory().getValidator();
	}
	/**
	 * @author danyuan
	 * @throws Exception 
	 */
	@Override
	public void preInvoke(List<ApiRequestParameter> requestParams, Method method,
			Class<?> targetClass) throws Throwable {
		//校验请求参数，规范客户端api对接人员输入
		if(log.isTraceEnabled()){
			log.trace("开始校验接口请求参数！");
		}
		//框架自动校验请求参数，并将处理结果以异常的方式抛出
		if(requestParams != null && requestParams.size() > 0){
			for(ApiRequestParameter parameter : requestParams){//依次校验所有参数
				Object param = parameter.getParam();
				if(!BeanPropertyUtil.isBaseType(param)){
					Set<ConstraintViolation<Object>>  errors=validator.validate(param);
					if(errors!=null&&(errors.size()>0)){
						BeanPropertyBindingResult result=new BeanPropertyBindingResult(param,param.getClass().getName());
						for(ConstraintViolation<Object> error:errors){
							String name=error.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
							FieldError item=new FieldError(param.getClass().getSimpleName(),error.getPropertyPath().toString(),
									 null, false, new String[]{name}, null,
									error.getMessage());				
							result.addError(item);
						}			
						throw new BindException(result);
					}
				}				
			}
		}		
				
	}

	/**
	 * @author danyuan
	 */
	@Override
	public Object afterInvoke(List<ApiRequestParameter> requestParams, Object resp,
			Method method, Class<?> targetClass) throws Throwable {
		//校验返回参数，规范服务端api开发人员输出
		if(log.isTraceEnabled()){
			log.trace("开始校验接口响应参数！");
		}
		if(resp != null){
			Set<ConstraintViolation<Object>>  errors=validator.validate(resp);
			if(errors!=null&&(errors.size()>0)){
				BeanPropertyBindingResult result=new BeanPropertyBindingResult(resp,resp.getClass().getName());
				for(ConstraintViolation<Object> error:errors){
					String name=error.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
					FieldError item=new FieldError(resp.getClass().getSimpleName(),error.getPropertyPath().toString(),
							 null, false, new String[]{name}, null,
							error.getMessage());				
					result.addError(item);
				}
				log.error("响应参数非法，请修正：{}",result.toString());
				throw new BindException(result);
			}
		}
		
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
