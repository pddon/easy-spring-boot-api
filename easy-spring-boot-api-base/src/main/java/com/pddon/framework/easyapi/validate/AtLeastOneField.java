package com.pddon.framework.easyapi.validate;

import java.lang.annotation.*;

/**
 * @ClassName: AtLeastOne
 * @Description: 多选一必填
 * @Author: Allen
 * @Date: 2024-04-22 22:01
 * @Addr: https://pddon.cn
 */
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneField {
    /**
     * 同一个分组内多选一必填
     * @return {@link String}
     * @author: Allen
     * @Date: 2024/4/22 22:42
     */
    String group() default "";

}
