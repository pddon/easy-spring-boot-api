/**  
* Title Decrypt.java
* Description  
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

import com.pddon.framework.easyapi.encrypt.DataEncryptHandler;
import com.pddon.framework.easyapi.encrypt.impl.AESDataEncryptHandler;

@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decrypt {
	/**
	 * 是否启用加密
	 * @author danyuan
	 */
	boolean value() default true;
	/**
	 * 手动指定加密处理器,默认采用AES加解密
	 * @author danyuan
	 */
	Class<? extends DataEncryptHandler> type() default AESDataEncryptHandler.class;
}
