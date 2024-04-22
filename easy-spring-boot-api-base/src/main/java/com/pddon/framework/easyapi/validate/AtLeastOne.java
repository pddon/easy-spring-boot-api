package com.pddon.framework.easyapi.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @ClassName: AtLeastOne
 * @Description: 多选一必填
 * @Author: Allen
 * @Date: 2024-04-22 22:01
 * @Addr: https://pddon.cn
 */
@Documented
@Constraint(validatedBy = AtLeastOneConstraintValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOne {
    String message() default "多选一必填分组参数不能全为空!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
