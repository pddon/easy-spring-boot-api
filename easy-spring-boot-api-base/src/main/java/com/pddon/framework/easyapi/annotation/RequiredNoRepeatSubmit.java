/**  
* Title DenyRepeatSubmit.java  
* Description  防重复提交
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.pddon.framework.easyapi.consts.DenyRepeatSubmitType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiredNoRepeatSubmit {
	boolean value() default true;
	DenyRepeatSubmitType mode() default DenyRepeatSubmitType.USE_SIGN;
}
