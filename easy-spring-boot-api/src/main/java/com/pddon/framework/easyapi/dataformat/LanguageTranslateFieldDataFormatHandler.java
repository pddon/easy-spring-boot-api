/**  
* Title LanguageTranslateFieldDataFormatHandler.java  
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.annotation.LanguageTranslate;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.utils.StringUtils;

@Component
@Slf4j
public class LanguageTranslateFieldDataFormatHandler extends AbstractFieldDataFormatHandler {

	@Autowired
	private LanguageTranslateManager languageTranslateManager;
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
		Annotation anno = annotations.get(LanguageTranslate.class);
		if(anno != null){
			LanguageTranslate languageTranslate = (LanguageTranslate) anno;
			if(languageTranslate.value()){
				if(data.getClass().equals(String.class)){
					String locale = languageTranslate.locale();
					if(StringUtils.isEmpty(locale)){
						locale = RequestContext.getContext().getLocale();
					}
					return languageTranslateManager.get(data.toString(), locale);
				}else{
					if(log.isTraceEnabled()){
						log.trace("@LanguageTranslate注解只能使用在String类型字段上,当前类型:{}!", data.getClass().getName());
					}										
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
