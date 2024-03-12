/**  
* Title DictManager.java  
* Description  
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi;

public interface LanguageTranslateManager {
	/**
	 * 获取某种语言的字典值
	 * @param key 错误码
	 * @param locale 语言
	 * @param params 参数列表，可以替换占位符,按顺序替换value里的%s
	 * @return
	 */
	public String get(String key,String locale,Object... params);
	/**
	 * 获取默认语言的字典值
	 * @param key 错误码
	 * @param params 参数列表，可以替换占位符,按顺序替换value里的%s
	 * @return
	 */
	public String get(String key,Object... params);
	/**
	 * 设置默认语言
	 * @param locale
	 */
	public void setDefaultLocale(String locale);
	
	/**
	 * 获取默认语言
	 * @param locale
	 */
	public String getDefaultLocale();
}
