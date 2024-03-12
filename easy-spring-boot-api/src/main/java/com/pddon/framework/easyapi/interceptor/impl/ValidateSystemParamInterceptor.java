/**  
* Title ApiLogInterceptor.java  
* Description  校验系统参数拦截器
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

import com.pddon.framework.easyapi.annotation.Encrypt;
import com.pddon.framework.easyapi.annotation.IgnoreResponseWrapper;
import com.pddon.framework.easyapi.annotation.RequiredCountryCode;
import com.pddon.framework.easyapi.annotation.RequiredCurrency;
import com.pddon.framework.easyapi.annotation.RequiredLocale;
import com.pddon.framework.easyapi.annotation.RequiredNoRepeatSubmit;
import com.pddon.framework.easyapi.annotation.RequiredParam;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotation.RequiredTimeZone;
import com.pddon.framework.easyapi.annotation.RequiredTimestamp;
import com.pddon.framework.easyapi.annotation.RequiredVersion;
import com.pddon.framework.easyapi.consts.DenyRepeatSubmitType;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.dto.ApiRestrictions;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.interceptor.AbstractApiMethodInterceptor;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.StringUtils;

@Service
@Slf4j
public class ValidateSystemParamInterceptor extends AbstractApiMethodInterceptor {
	
	@Autowired
	private SystemParameterRenameProperties systemParameterProperties;

	/**
	 * @author danyuan
	 */
	@Override
	public void preInvoke(List<ApiRequestParameter> requestParams, Method method,
			Class<?> targetClass) throws Throwable {
		if(log.isTraceEnabled()){
			log.trace("开始校验系统参数！");
		}
		ApiRestrictions restrictions = new ApiRestrictions();
		//设置接口响应是否需要包装标志
		IgnoreResponseWrapper ignoreResponseWrapper = method.getAnnotation(IgnoreResponseWrapper.class);
		if(ignoreResponseWrapper != null){
			restrictions.setIgnoreResponseWrapper(ignoreResponseWrapper.value());
		}
		RequestContext.getContext().setApiRestrictions(restrictions);
		//设置接口响应内容是否需要加密
		Encrypt encrypt = method.getAnnotation(Encrypt.class);
		if(encrypt != null && encrypt.value()){
			restrictions.setEncrptResponseData(encrypt.value());
			restrictions.setEncrptHandlerClass(encrypt.type());			
		}
				
		//校验会话信息
		RequiredSession requiredSession=method.getAnnotation(RequiredSession.class);
		if(requiredSession != null && requiredSession.value()){
			String sessionId = RequestContext.getContext().getSessionId();
			if(StringUtils.isBlank(sessionId)){
				throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getSessionId());
			}			
			restrictions.setSession(requiredSession.value());
		}
		//检查多语言
		RequiredLocale requiredLocale=method.getAnnotation(RequiredLocale.class);
		if(requiredLocale != null && requiredLocale.value()){
			String locale = RequestContext.getContext().getLocale();
			if(StringUtils.isBlank(locale)){
				throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getLocale());
			}
		}
		//检查多语言
		RequiredCountryCode requiredCountryCode=method.getAnnotation(RequiredCountryCode.class);
		if(requiredCountryCode != null && requiredCountryCode.value()){
			String countryCode = RequestContext.getContext().getCountryCode();
			if(StringUtils.isBlank(countryCode)){
				throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getCountryCode());
			}
		}	
		//检查货币类型
		RequiredCurrency requiredCurrency=method.getAnnotation(RequiredCurrency.class);
		if(requiredCurrency != null && requiredCurrency.value()){
			String currency = RequestContext.getContext().getCurrency();
			if(StringUtils.isBlank(currency)){
				throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getCurrency());
			}
		}
		//检查时间戳
		RequiredTimestamp requiredTimestamp=method.getAnnotation(RequiredTimestamp.class);
		if(requiredTimestamp != null && requiredTimestamp.value()){
			String timestamp = RequestContext.getContext().getTimestamp();
			if(StringUtils.isBlank(timestamp)){
				throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getTimestamp());
			}
		}
		//检查时区
		RequiredTimeZone requiredTimeZone=method.getAnnotation(RequiredTimeZone.class);
		if(requiredTimeZone != null && requiredTimeZone.value()){
			String timeZone = RequestContext.getContext().getTimeZone();
			if(StringUtils.isBlank(timeZone)){
				throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getTimeZone());
			}
		}
		//检查数字签名
		RequiredSign requiredSign=method.getAnnotation(RequiredSign.class);
		if(requiredSign != null && requiredSign.value()){
			if(SignScope.REQUEST.equals(requiredSign.scope())){
				restrictions.setRequestSign(requiredSign.value());
			}else if(SignScope.RESPONSE.equals(requiredSign.scope())){
				restrictions.setResponseSign(requiredSign.value());
			}else{
				restrictions.setRequestSign(requiredSign.value());
				restrictions.setResponseSign(requiredSign.value());
			}
			if(restrictions.getRequestSign()){
				String sign = RequestContext.getContext().getSign();
				if(StringUtils.isBlank(sign)){
					throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getSign());
				}
				if(StringUtils.isBlank(RequestContext.getContext().getTimestamp())){
					throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getTimestamp());
				}
			}
			
		}	
		//检查版本号
		RequiredVersion requiredVersion=method.getAnnotation(RequiredVersion.class);
		if(requiredVersion != null && requiredVersion.value()){
			String version = RequestContext.getContext().getVersionCode();
			if(StringUtils.isBlank(version)){
				throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getVersionCode());
			}
		}
		//校验防重复提交码
		RequiredNoRepeatSubmit requiredNoRepeatSubmit=method.getAnnotation(RequiredNoRepeatSubmit.class);
		if(requiredNoRepeatSubmit != null && requiredNoRepeatSubmit.value()){	
			String timestamp = RequestContext.getContext().getTimestamp();
			if(StringUtils.isBlank(timestamp)){
				throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getTimestamp());
			}
			
			if(DenyRepeatSubmitType.USE_SIGN.equals(requiredNoRepeatSubmit.mode())){
				String sign = RequestContext.getContext().getSign();
				if(StringUtils.isBlank(sign)){
					throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getSign());
				}
				RequestContext.getContext().setAttachment(
						SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.REPEAT_CODE), sign);
			}
			if(DenyRepeatSubmitType.GENERATE_TOKEN.equals(requiredNoRepeatSubmit.mode())){				
				String repeatCode = RequestContext.getContext().getRepeatCode();
				if(StringUtils.isBlank(repeatCode)){
					throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(systemParameterProperties.getRepeatCode());
				}
			}			
			restrictions.setUniqueSubmit(requiredNoRepeatSubmit.value());
		}
		
		//校验其他系统参数
		RequiredParam requiredParam=method.getAnnotation(RequiredParam.class);
		if(requiredParam != null){
			String[] names = requiredParam.value();
			String value = null;
			for(String name : names){
				if(!SystemParameterRenameProperties.DEFAULT_PARAM_MAP.containsKey(name)){
					log.warn("系统参数:[{}]需要在配置文件中先配置，否则无法获取该配置信息!", name);
				}else{
					value = RequestContext.getContext().getAttachment(name);
					if(StringUtils.isBlank(value)){
						throw new BusinessException(ErrorCodes.PARAM_LOST).setParam(name);
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
		return resp;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public int order() {
		return -3;
	}

}
