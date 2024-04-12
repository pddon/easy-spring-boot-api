/**  
* Title EnableMongoPlus.java  
* Description  
* @author danyuan
* @date Mar 13, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.annotation;

import com.pddon.framework.easyapi.config.EasyApiAsyncConfigurer;
import org.springframework.context.annotation.Import;

import com.pddon.framework.easyapi.config.EasyApiBeanConfigurer;
import com.pddon.framework.easyapi.config.EasyApiSwaggerConfigurer;
import com.pddon.framework.easyapi.config.EasyApiWebConfigurer;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EasyApiWebConfigurer.class, EasyApiSwaggerConfigurer.class, EasyApiBeanConfigurer.class, EasyApiAsyncConfigurer.class})
public @interface EnableEasyApi {
    
}
