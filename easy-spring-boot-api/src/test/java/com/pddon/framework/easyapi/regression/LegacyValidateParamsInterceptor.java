package com.pddon.framework.easyapi.regression;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;

/**
 * HEAD(b766ccd) 中 ValidateParamsInterceptor 的逐行副本，仅用于回归对照测试。
 */
final class LegacyValidateParamsInterceptor {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private LegacyValidateParamsInterceptor() {
    }

    static Validator validator() {
        return VALIDATOR;
    }

    static void preInvoke(List<ApiRequestParameter> requestParams, Method method, Class<?> targetClass) throws Throwable {
        if (requestParams != null && requestParams.size() > 0) {
            for (ApiRequestParameter parameter : requestParams) {//依次校验所有参数
                Object param = parameter.getParam();
                if (!BeanPropertyUtil.isBaseType(param)) {
                    Set<ConstraintViolation<Object>> errors = VALIDATOR.validate(param);
                    if (errors != null && (errors.size() > 0)) {
                        BeanPropertyBindingResult result = new BeanPropertyBindingResult(param, param.getClass().getName());
                        for (ConstraintViolation<Object> error : errors) {
                            String name = error.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
                            FieldError item = new FieldError(param.getClass().getSimpleName(), error.getPropertyPath().toString(),
                                    null, false, new String[]{name}, null,
                                    error.getMessage());
                            result.addError(item);
                        }
                        throw new BindException(result);
                    }
                }
            }
        }
    }

    static Object afterInvoke(List<ApiRequestParameter> requestParams, Object resp,
                              Method method, Class<?> targetClass) throws Throwable {
        if (resp != null) {
            Set<ConstraintViolation<Object>> errors = VALIDATOR.validate(resp);
            if (errors != null && (errors.size() > 0)) {
                BeanPropertyBindingResult result = new BeanPropertyBindingResult(resp, resp.getClass().getName());
                for (ConstraintViolation<Object> error : errors) {
                    String name = error.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
                    FieldError item = new FieldError(resp.getClass().getSimpleName(), error.getPropertyPath().toString(),
                            null, false, new String[]{name}, null,
                            error.getMessage());
                    result.addError(item);
                }
                throw new BindException(result);
            }
        }
        return resp;
    }
}
