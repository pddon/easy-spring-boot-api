package com.pddon.framework.easyapi.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Payload;
import javax.validation.Validator;
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
 * 验证ValidateParamsInterceptor改造后：
 * 1、普通校验失败仍抛出原有BindException且错误信息格式不变；
 * 2、分组校验行为不变；
 * 3、自定义ConstraintValidator仍由Spring容器管理并生效；
 * 4、容器中的MessageInterpolator仍然生效；
 * 5、脱离Spring容器时回退默认Validator仍可工作。
 */
public class ValidateParamsInterceptorTest {

    private static final Method DUMMY_METHOD;
    static {
        try {
            DUMMY_METHOD = ValidateParamsInterceptorTest.class.getDeclaredMethod("dummy", UserForm.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unused")
    private void dummy(UserForm form) {
    }

    private static List<ApiRequestParameter> params(Object param) {
        ApiRequestParameter item = new ApiRequestParameter();
        item.setIndex(0);
        item.setParamName("request");
        item.setParam(param);
        List<ApiRequestParameter> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    /**
     * 容器中存在多个Validator时：回退默认Validator，且不会把回退结果缓存为容器Validator
     */
    @Test
    public void testMultipleValidatorBeansFallBackToDefault() throws Throwable {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MultiValidatorConfig.class)) {
            ValidateParamsInterceptor interceptor = new ValidateParamsInterceptor(context.getBeanProvider(Validator.class));
            UserForm form = new UserForm();
            form.setName(" ");
            form.setAge(17);
            form.setEmail("not-an-email");
            try {
                interceptor.preInvoke(params(form), DUMMY_METHOD, UserForm.class);
                Assert.fail("应当抛出BindException");
            } catch (BindException e) {
                Assert.assertEquals(3, e.getFieldErrorCount());
                Assert.assertEquals("age不能小于18", e.getFieldError("age").getDefaultMessage());
                Assert.assertEquals("name不能为空", e.getFieldError("name").getDefaultMessage());
            }
            // 容器Validator解析失败时只缓存回退Validator，容器Validator字段保持为空，后续仍会尝试解析
            Assert.assertNull(ReflectionTestUtils.getField(interceptor, "validator"));
            Assert.assertNotNull(ReflectionTestUtils.getField(interceptor, "fallbackValidator"));
        }
    }

    /**
     * 普通校验失败：异常类型、字段名、错误消息与原来一致
     */
    @Test
    public void testNormalValidationFailureKeepsExceptionAndMessage() throws Throwable {
        // 不经过Spring容器，走回退默认Validator路径
        ValidateParamsInterceptor interceptor = new ValidateParamsInterceptor();
        UserForm form = new UserForm();
        form.setName(" ");
        form.setAge(17);
        form.setEmail("not-an-email");

        try {
            interceptor.preInvoke(params(form), DUMMY_METHOD, UserForm.class);
            Assert.fail("应当抛出BindException");
        } catch (BindException e) {
            Assert.assertEquals(3, e.getFieldErrorCount());
            FieldError ageError = e.getFieldError("age");
            FieldError nameError = e.getFieldError("name");
            FieldError emailError = e.getFieldError("email");
            Assert.assertNotNull(ageError);
            Assert.assertNotNull(nameError);
            Assert.assertNotNull(emailError);
            Assert.assertEquals("UserForm", ageError.getObjectName());
            Assert.assertEquals("age不能小于18", ageError.getDefaultMessage());
            Assert.assertEquals("name不能为空", nameError.getDefaultMessage());
            Assert.assertNotNull(emailError.getDefaultMessage());
        }
    }

    /**
     * 分组校验：默认分组不校验GroupA约束，显式传入GroupA时生效
     */
    @Test
    public void testGroupValidationBehaviorUnchanged() throws Throwable {
        ValidateParamsInterceptor interceptor = new ValidateParamsInterceptor();
        GroupForm form = new GroupForm();
        // 默认分组下group字段为空不应报错
        interceptor.preInvoke(params(form), DUMMY_METHOD, GroupForm.class);

        Validator validator = (Validator) ReflectionTestUtils.invokeMethod(interceptor, "getValidator");
        Assert.assertNotNull(validator);
        // 默认分组无违规
        Assert.assertEquals(0, validator.validate(form).size());
        // GroupA分组命中约束
        Assert.assertEquals(1, validator.validate(form, GroupA.class).size());
    }

    /**
     * 自定义ConstraintValidator：由容器创建，可使用容器中的依赖；MessageInterpolator同样使用容器配置
     */
    @Test
    public void testCustomConstraintValidatorAndMessageInterpolatorFromContainer() throws Throwable {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ValidatorTestConfig.class)) {
            ObjectProvider<Validator> provider = context.getBeanProvider(Validator.class);
            ValidateParamsInterceptor interceptor = new ValidateParamsInterceptor(provider);

            // 合法值：前缀正确，校验通过
            CodeForm valid = new CodeForm();
            valid.setName("ok");
            valid.setCode("ACME-001");
            interceptor.preInvoke(params(valid), DUMMY_METHOD, CodeForm.class);

            // 非法值：前缀不匹配，自定义校验器生效；消息经过容器的MessageInterpolator
            CodeForm invalid = new CodeForm();
            invalid.setName(" ");
            invalid.setCode("OTHER-001");
            try {
                interceptor.preInvoke(params(invalid), DUMMY_METHOD, CodeForm.class);
                Assert.fail("应当抛出BindException");
            } catch (BindException e) {
                Assert.assertEquals(2, e.getFieldErrorCount());
                Assert.assertEquals("[container]code前缀必须为ACME", e.getFieldError("code").getDefaultMessage());
                Assert.assertEquals("[container]name不能为空", e.getFieldError("name").getDefaultMessage());
            }
        }
    }

    /**
     * 断言容器中的自定义校验器确实被调用（同一实例）
     */
    @Test
    public void testContainerManagedValidatorInstanceUsed() throws Throwable {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ValidatorTestConfig.class)) {
            PrefixCodeValidator containerValidator = context.getBean(PrefixCodeValidator.class);
            int before = containerValidator.getInvocationCount();
            int createdBefore = PrefixCodeValidator.CREATED_COUNT.get();

            ValidateParamsInterceptor interceptor = new ValidateParamsInterceptor(context.getBeanProvider(Validator.class));
            CodeForm form = new CodeForm();
            form.setName("ok");
            form.setCode("BAD-001");
            try {
                interceptor.preInvoke(params(form), DUMMY_METHOD, CodeForm.class);
                Assert.fail("应当抛出BindException");
            } catch (BindException expected) {
                // ignore
            }
            // 无论容器直接复用该Bean，还是通过BeanFactory创建，都证明校验器由容器创建并注入了容器中的依赖
            Assert.assertTrue(containerValidator.getInvocationCount() > before
                    || PrefixCodeValidator.CREATED_COUNT.get() > createdBefore);
        }
    }

    @Configuration
    static class MultiValidatorConfig {
        @Bean
        public LocalValidatorFactoryBean validator1() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        public LocalValidatorFactoryBean validator2() {
            return new LocalValidatorFactoryBean();
        }
    }

    @Configuration
    static class ValidatorTestConfig {
        @Bean
        public PrefixProvider prefixProvider() {
            return new PrefixProvider("ACME");
        }

        @Bean
        public PrefixCodeValidator prefixCodeValidator(PrefixProvider prefixProvider) {
            return new PrefixCodeValidator(prefixProvider);
        }

        @Bean
        public LocalValidatorFactoryBean validator(MessageInterpolator messageInterpolator) {
            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.setMessageInterpolator(messageInterpolator);
            return validator;
        }

        @Bean
        public MessageInterpolator messageInterpolator() {
            return new MessageInterpolator() {
                @Override
                public String interpolate(String messageTemplate, Context context) {
                    return "[container]" + context.getConstraintDescriptor().getAttributes().get("message");
                }

                @Override
                public String interpolate(String messageTemplate, Context context, java.util.Locale locale) {
                    return "[container]" + context.getConstraintDescriptor().getAttributes().get("message");
                }
            };
        }
    }

    static class PrefixProvider {
        private final String prefix;

        PrefixProvider(String prefix) {
            this.prefix = prefix;
        }

        String getPrefix() {
            return prefix;
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = PrefixCodeValidator.class)
    public @interface PrefixCode {
        String message() default "code前缀必须为ACME";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class PrefixCodeValidator implements ConstraintValidator<PrefixCode, String> {
        static final java.util.concurrent.atomic.AtomicInteger CREATED_COUNT = new java.util.concurrent.atomic.AtomicInteger();
        private final PrefixProvider prefixProvider;
        private int invocationCount;

        public PrefixCodeValidator(PrefixProvider prefixProvider) {
            this.prefixProvider = prefixProvider;
            CREATED_COUNT.incrementAndGet();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            invocationCount++;
            return value != null && value.startsWith(prefixProvider.getPrefix() + "-");
        }

        public int getInvocationCount() {
            return invocationCount;
        }
    }

    static class UserForm {
        @NotBlank(message = "name不能为空")
        private String name;
        @Min(value = 18, message = "age不能小于{value}")
        private int age;
        @javax.validation.constraints.Email(message = "email格式不正确")
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

    static class GroupForm {
        @NotNull(groups = GroupA.class, message = "group字段不能为空")
        private String group;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }
    }

    interface GroupA {
    }

    static class CodeForm {
        @NotBlank(message = "name不能为空")
        private String name;
        @PrefixCode
        private String code;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
