/**
 * Title OperateLog.java
 * Description  记录操作日志
 * @author Allen
 * @date Dec 19, 2023
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {
    @AliasFor("type")
    String value() default "通用记录";

    /**
     * 操作日志类型
     * @author Allen
     */
    @AliasFor("value")
    String type() default "通用记录";

    /**
     * 接口名
     * @return {@link String}
     * @author: Allen
     * @Date: 2024/4/19 17:25
     */
    String apiName() default "";
}
