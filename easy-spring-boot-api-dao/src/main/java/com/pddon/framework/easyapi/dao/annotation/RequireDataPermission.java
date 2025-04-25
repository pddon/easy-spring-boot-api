package com.pddon.framework.easyapi.dao.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: RequireDataPermission
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-24 22:39
 * @Addr: https://pddon.cn
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireDataPermission {
    /**
     * 是否启用数据权限校验
     * @return {@link boolean}
     * @author: Allen
     * @Date: 2025/4/24 23:04
     */
    boolean value() default true;

    /**
     * 需要校验的数据权限字段，{表名}.{字段名}，通过此字段去查找当前登录用户的数据权限值<br>
     * eg. User.userId
     * @return {@link String[]}
     * @author: Allen
     * @Date: 2025/4/24 23:05
     */
    String[] tableFields() default {};

    /**
     * 数据权限字段全名<br>
     * eg. u.userId 或者 userId
     * @return {@link String[]}
     * @author: Allen
     * @Date: 2025/4/24 23:05
     */
    String[] tableFieldAlias() default {};

}
