/**  
* Title EncryptFieldDataFormatHandler.java  
* Description  
* @author danyuan
* @date Nov 16, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dataformat;

import java.lang.annotation.Annotation;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.pddon.framework.easyapi.annotation.Encrypt;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.encrypt.DataEncryptHandler;
import com.pddon.framework.easyapi.utils.SpringBeanUtil;
import com.pddon.framework.easyapi.utils.StringUtils;

@Component
@Slf4j
public class EncryptFieldDataFormatHandler extends AbstractFieldDataFormatHandler{

	/**
	 * @author danyuan
	 */
	@Override
	public int order() {
		return 0;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public Object handle(String fieldName, Object data, Map<Class<?>, Annotation> annotations)
			throws Throwable {
		if(StringUtils.isBlank(data)){
			return data;
		}
		Annotation anno = annotations.get(Encrypt.class);
		if(anno != null){
			Encrypt encrypt = (Encrypt) anno;
			if(encrypt.value()){
				DataEncryptHandler dataEncryptHandler = SpringBeanUtil.getBean(encrypt.type());
				if(dataEncryptHandler != null){
					if(data.getClass().equals(String.class)){
						return dataEncryptHandler.encrypt(RequestContext.getContext().getAppId(), 
								RequestContext.getContext().getChannelId(), 
								RequestContext.getContext().getUserId(), 
								data.toString());
					}else{
						if(log.isTraceEnabled()){
							log.trace("@Encrypt注解只能使用在String类型字段上,当前类型:{}!", data.getClass().getName());
						}										
					}									
				}else{
					log.warn("spring 容器中未找到 此类{}的实例对象!", encrypt.type());
				}
			}
		}		
		return data;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public boolean handleRequest() {
		return false;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public boolean handleResponse() {
		return true;
	}

}
