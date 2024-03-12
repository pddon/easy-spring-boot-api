/**  
* Title LanguageTranslate.java  
* Description  国际化语言翻译注解
* @author danyuan
* @date Nov 15, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.FIELD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LanguageTranslate {
	/**
	 * 特定的语言
	 * @author danyuan
	 */
	String locale() default "";
	/**
	 * 是否需要翻译
	 * @author danyuan
	 */
	boolean value() default true;
	
}
