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

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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

	/**
	 * Spring容器中的Validator提供者，延迟解析，确保使用容器管理的Validator
	 * （自定义ConstraintValidator、MessageInterpolator、国际化配置均保持容器中的行为）；
	 * 脱离Spring容器手动创建本拦截器时为null，回退到默认Validator。
	 */
	private final ObjectProvider<Validator> validatorProvider;

	/**
	 * 容器中解析到的Validator缓存；只缓存成功解析的结果
	 */
	private volatile Validator validator;

	/**
	 * 回路使用的默认Validator缓存（非Spring环境或容器中无唯一Validator时使用），
	 * 避免每次校验都重复构建ValidatorFactory
	 */
	private volatile Validator fallbackValidator;

	/**
	 * 容器中存在多个Validator时只提示一次，避免日志刷屏
	 */
	private static final AtomicBoolean AMBIGUOUS_VALIDATOR_WARNED = new AtomicBoolean(false);

	/**
	 * 供Spring容器注入使用：延迟获取容器中的Validator，取不到时回退到默认实现
	 * @author danyuan
	 */
	@Autowired
	public ValidateParamsInterceptor(ObjectProvider<Validator> validatorProvider) {
		this.validatorProvider = validatorProvider;
	}

	/**
	 * 脱离Spring容器使用时的兼容构造器，保持原有行为
	 * @author danyuan
	 */
	public ValidateParamsInterceptor() {
		this.validatorProvider = null;
		this.validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	/**
	 * 获取Validator：优先使用Spring容器中的Validator，容器中不存在时回退到默认Validator。
	 * 容器中的Validator仅在成功解析后才缓存，避免把容器尚未就绪时的一次失败固化成永久结果。
	 */
	private Validator getValidator() {
		Validator current = this.validator;
		if (current != null) {
			return current;
		}
		if (this.validatorProvider != null) {
			try {
				Validator containerValidator = this.validatorProvider.getIfAvailable();
				if (containerValidator != null) {
					this.validator = containerValidator;
					return containerValidator;
				}
			} catch (NoUniqueBeanDefinitionException e) {
				//容器中存在多个Validator，无法确定使用哪一个，回退到默认Validator并提示一次
				if (AMBIGUOUS_VALIDATOR_WARNED.compareAndSet(false, true)) {
					log.warn("Spring容器中存在多个Validator，无法唯一确定用于参数校验的Validator，"
							+ "将回退到默认Validator；如需使用容器中的Validator，请保证容器中只有一个Validator Bean。");
				}
			}
		}
		Validator fallback = this.fallbackValidator;
		if (fallback == null) {
			fallback = Validation.buildDefaultValidatorFactory().getValidator();
			this.fallbackValidator = fallback;
		}
		return fallback;
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
			Validator validator = getValidator();
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
			Validator validator = getValidator();
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
