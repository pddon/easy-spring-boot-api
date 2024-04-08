/**  
* Title RequiredParam.java  
* Description  校验必填的系统参数
* @author danyuan
* @date Nov 8, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 配置
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiredParam {
	/**
	 * 参数名数组，可以同时配置忽略多个参数
	 * @author danyuan
	 */
	String[] value() default {};
	/**
	 * 是否允许空字符串, 默认不允许
	 * @author danyuan
	 */
	boolean allowBlank() default false;
}
