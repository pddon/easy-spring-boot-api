/**  
* Title DefaultLanguageTranslateManagerImpl.java  
* Description  默认字典翻译器，默认使用spring容器i18n字典翻译器
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.impl;

import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import com.pddon.framework.easyapi.LanguageTranslateManager;

@Setter
@AllArgsConstructor
@Slf4j
public class DefaultLanguageTranslateManagerImpl implements LanguageTranslateManager{

	private MessageSource messageSource;
	
	private Locale locale;
	
	/**
	 * @author danyuan
	 */
	@Override
	public String get(String key, String localeName, Object... params) {
		
		try{
			String[] arr = localeName.split("_");
			Locale locale = new Locale(arr[0], arr[1]);
			return messageSource.getMessage(key, params, locale);
		}catch(NoSuchMessageException e){
			return key;
		}catch(Exception e){
			log.warn("Unkown locale [{}]", localeName);
			return key;
		}		
	}

	/**
	 * @author danyuan
	 */
	@Override
	public String get(String key, Object... params) {
		try{
			String[] arr = this.getDefaultLocale().split("_");
			Locale locale = new Locale(arr[0], arr[1]);
			return messageSource.getMessage(key, params, locale);
		}catch(NoSuchMessageException e){
			return key;
		}catch(Exception e){
			log.warn("Unkown default locale [{}]", this.getDefaultLocale());
			return key;
		}
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void setDefaultLocale(String locale) {
		this.locale = new Locale(locale);
	}

	/**
	 * @author danyuan
	 */
	@Override
	public String getDefaultLocale() {
		return locale.toString();
	}

}
