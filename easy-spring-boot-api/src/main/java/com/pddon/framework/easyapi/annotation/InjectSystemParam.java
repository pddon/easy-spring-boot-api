/**  
* Title InjectSystemParam.java
* Description  自动将系统参数值赋值给某些字段
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

@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InjectSystemParam {
	/**
	 * 参数列表，默认为所有都拦截
	 * @author danyuan
	 */
	String[] value() default {};
	/**
	 * 排除某些系统参数
	 * @author danyuan
	 */
	String[] execludes() default {};
}
