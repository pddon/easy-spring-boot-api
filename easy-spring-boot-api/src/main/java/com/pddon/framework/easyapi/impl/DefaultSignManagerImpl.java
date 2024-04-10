/**  
* Title DefaultSignManagerImpl.java  
* Description  
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pddon.framework.easyapi.annotation.IgnoreSign;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.encrypt.SignEncryptHandler;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.EncryptUtils;
import com.pddon.framework.easyapi.utils.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;

import com.pddon.framework.easyapi.SignManager;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;

@Slf4j
public class DefaultSignManagerImpl implements SignManager{

	/**
	 * @author danyuan
	 */
	@Override
	public String sign(String secret, String token, List<ApiRequestParameter> params) {
		final Map<String, String> nameValueMap = new HashMap<>();
		for(ApiRequestParameter param : params){
			if(param.isNeedSign()){
				Object value = param.getParam();
				if(value.getClass().isEnum()){
					value = ((Enum)value).name();
				}
				String key = null;
				if(BeanPropertyUtil.isBaseType(value)){
					key = param.getParamName();
				}
				nameValueMap.putAll(BeanPropertyUtil.objToStringMap(value, key, IgnoreSign.class));
			}
		}
		//剔除系统参数
		Map<String, String> map = nameValueMap.keySet().stream()
				.filter(key -> !SystemParameterRenameProperties.DEFAULT_PARAM_MAP.containsKey(key))
				.collect(Collectors.toMap(key->key, key -> nameValueMap.get(key)));
		//按key进行字符串自然序排序后，进行拼接
		String content = EncryptUtils.sortAndMontage(map);
		if(log.isTraceEnabled()){
			log.trace("token:[{}],params to string sort result:[{}]", token, content);
		}
		//拼接随机token码
		String data = token + content + token;
		Method method = RequestContext.getContext().getMethod();
		RequiredSign requiredSign =method.getAnnotation(RequiredSign.class);
		SignEncryptHandler signEncryptHandler = SpringBeanUtil.getBean(requiredSign.type());
		if(signEncryptHandler != null){
			//进行数字签名
			return signEncryptHandler.sign(secret, data);
		}else{
			log.warn("数字签名生成器[{}]实例未找到,请先配置!", requiredSign.type().toString());
			throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam(requiredSign.type().toString());
		} 
	}

	/**
	 * @author danyuan
	 */
	@Override
	public boolean checkSign(String sign, String secret, String token,
			List<ApiRequestParameter> params) {
		return sign.equals(this.sign(secret, token, params));
	}

}
