/**  
* Title IgnoreResponseWrapper.java  
* Description  此接口不需要进行包装
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
 * 当接口需要返回自定义原始内容时，开启此注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreResponseWrapper {
	boolean value() default true;
}
