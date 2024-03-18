package com.pddon.framework.easyapi.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: ReplaceRoute
 * @Description:
 * @Author: Allen
 * @Date: 2024-03-18 21:45
 * @Addr: https://pddon.cn
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ReplaceRoute {
    String value() default "";
}

