/**  
* Title IgnoreTenant.java
* Description  租户表需要忽略的字段
* @author danyuan
* @date Nov 8, 2024
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dao.annotation;

import java.lang.annotation.*;

/**
 * 被标注的实体类视为非租户表，被标注的业务类或方法下sql自动忽略租户过滤条件
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreTenant {
	boolean value() default true;
}
