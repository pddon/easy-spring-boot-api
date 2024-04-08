/**  
* Title IgnoreSign.java  
* Description  签名需要忽略的字段
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
 * 当接口开启验签时，添加此注解的字段不参与签名
 */
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreSign {
	boolean value() default true;
}
