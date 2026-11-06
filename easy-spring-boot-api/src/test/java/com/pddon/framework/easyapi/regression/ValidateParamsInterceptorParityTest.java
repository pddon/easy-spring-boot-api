package com.pddon.framework.easyapi.regression;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validator;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.interceptor.impl.ValidateParamsInterceptor;

/**
 * 优化点8的对照测试：ValidateParamsInterceptor 旧 validate 逻辑 vs 生产实现。
 */
public class ValidateParamsInterceptorParityTest {

    @Test
    public void testPlainValidationMatchesLegacy() throws Throwable {
        ValidateParamsInterceptor interceptor = new ValidateParamsInterceptor();
        PlainForm form = new PlainForm();
        form.setName("");
        form.setAge(1);
        form.setEmail("not-an-email");

        List<ApiRequestParameter> params = params(form);
        BindException legacy = captureLegacy(params);
        BindException produced = captureProduced(interceptor, params);
        assertBindParity(legacy, produced);
        Assert.assertEquals(3, produced.getFieldErrorCount());
    }

    @Test
    public void testGroupValidationMatchesLegacy() throws Throwable {
        ValidateParamsInterceptor interceptor = new ValidateParamsInterceptor();
        GroupForm form = new GroupForm();
        form.setGroupValue(null);
        form.setDefaultValue("default-ok");

        List<ApiRequestParameter> params = params(form);
        Assert.assertNull(captureLegacy(params));
        Assert.assertNull(captureProduced(interceptor, params));

        Validator producedValidator = (Validator) ReflectionTestUtils.invokeMethod(interceptor, "getValidator");
        Validator legacyValidator = LegacyValidateParamsInterceptor.validator();
        Set<ConstraintViolation<GroupForm>> legacyErrors = legacyValidator.validate(form, GroupA.class);
        Set<ConstraintViolation<GroupForm>> producedErrors = producedValidator.validate(form, GroupA.class);
        Assert.assertEquals(violationSignatures(legacyErrors), violationSignatures(producedErrors));
        Assert.assertEquals(1, producedErrors.size());
    }

    @Test
    public void testCustomConstraintValidatorMatchesLegacy() throws Throwable {
        ValidateParamsInterceptor interceptor = new ValidateParamsInterceptor();
        CodeForm form = new CodeForm();
        form.setCode("wrong-prefix");

        List<ApiRequestParameter> params = params(form);
        BindException legacy = captureLegacy(params);
        BindException produced = captureProduced(interceptor, params);
        assertBindParity(legacy, produced);
        Assert.assertEquals(1, produced.getFieldErrorCount());
        Assert.assertEquals("必须以PFX_开头", produced.getFieldError("code").getDefaultMessage());
    }

    @Test
    public void testMultipleContainerValidatorsFallBackToDefaultLikeLegacy() throws Throwable {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MultiValidatorConfig.class);
        try {
            ObjectProvider<Validator> provider = context.getBeanProvider(Validator.class);
            ValidateParamsInterceptor interceptor = new ValidateParamsInterceptor(provider);
            PlainForm form = new PlainForm();
            form.setName("");
            form.setAge(1);
            form.setEmail("bad");

            List<ApiRequestParameter> params = params(form);
            BindException legacy = captureLegacy(params);
            BindException produced = captureProduced(interceptor, params);
            assertBindParity(legacy, produced);
        } finally {
            context.close();
        }
    }

    private static BindException captureLegacy(List<ApiRequestParameter> params) {
        try {
            LegacyValidateParamsInterceptor.preInvoke(params, dummyMethod(), Object.class);
            return null;
        } catch (BindException e) {
            return e;
        } catch (Throwable e) {
            throw new AssertionError("旧实现抛出非预期异常", e);
        }
    }

    private static BindException captureProduced(ValidateParamsInterceptor interceptor,
                                                 List<ApiRequestParameter> params) throws Throwable {
        try {
            interceptor.preInvoke(params, dummyMethod(), Object.class);
            return null;
        } catch (BindException e) {
            return e;
        }
    }

    private static void assertBindParity(BindException legacy, BindException produced) {
        if (legacy == null || produced == null) {
            Assert.assertNull(legacy);
            Assert.assertNull(produced);
            return;
        }
        Assert.assertEquals(legacy.getClass(), produced.getClass());
        Assert.assertEquals(legacy.getObjectName(), produced.getObjectName());
        Assert.assertEquals(legacy.getErrorCount(), produced.getErrorCount());
        Assert.assertEquals(legacy.getFieldErrorCount(), produced.getFieldErrorCount());
        Assert.assertEquals(fieldSignatures(legacy), fieldSignatures(produced));
    }

    private static List<String> fieldSignatures(BindException exception) {
        List<String> signatures = new ArrayList<>();
        for (FieldError error : exception.getFieldErrors()) {
            signatures.add(error.getField() + "|" + error.getDefaultMessage() + "|" + error.getCode());
        }
        Collections.sort(signatures);
        return signatures;
    }

    private static <T> List<String> violationSignatures(Set<ConstraintViolation<T>> violations) {
        List<String> signatures = new ArrayList<>();
        for (ConstraintViolation<T> violation : violations) {
            signatures.add(violation.getPropertyPath().toString() + "|"
                    + violation.getMessage() + "|"
                    + violation.getConstraintDescriptor().getAnnotation().annotationType().getName());
        }
        Collections.sort(signatures);
        return signatures;
    }

    private static List<ApiRequestParameter> params(Object param) {
        List<ApiRequestParameter> params = new ArrayList<>();
        ApiRequestParameter parameter = new ApiRequestParameter();
        parameter.setIndex(0);
        parameter.setParamName("param");
        parameter.setParam(param);
        params.add(parameter);
        return params;
    }

    private static Method dummyMethod() {
        try {
            return ValidateParamsInterceptorParityTest.class.getDeclaredMethod("dummy", Object.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unused")
    private void dummy(Object param) {
    }

    public interface GroupA {
    }

    public static class PlainForm {
        @NotBlank(message = "名称不能为空")
        private String name;
        @Min(value = 18, message = "年龄不能小于18")
        private int age;
        @Email(message = "邮箱格式不正确")
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class GroupForm {
        @NotNull(groups = GroupA.class, message = "分组值不能为空")
        private String groupValue;
        @NotNull(message = "默认值不能为空")
        private String defaultValue;

        public String getGroupValue() {
            return groupValue;
        }

        public void setGroupValue(String groupValue) {
            this.groupValue = groupValue;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    public static class CodeForm {
        @PrefixCode
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = PrefixCodeValidator.class)
    public @interface PrefixCode {
        String message() default "必须以PFX_开头";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class PrefixCodeValidator implements ConstraintValidator<PrefixCode, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value != null && value.startsWith("PFX_");
        }
    }

    @Configuration
    public static class MultiValidatorConfig {
        @Bean
        public LocalValidatorFactoryBean firstValidator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        public LocalValidatorFactoryBean secondValidator() {
            return new LocalValidatorFactoryBean();
        }
    }
}
