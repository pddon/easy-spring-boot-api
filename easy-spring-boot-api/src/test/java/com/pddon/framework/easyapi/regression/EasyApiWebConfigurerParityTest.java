package com.pddon.framework.easyapi.regression;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.pddon.framework.easyapi.config.EasyApiWebConfigurer;
import com.pddon.framework.easyapi.properties.EasyApiConfig;

/**
 * 优化点7的对照测试：消息转换器列表与 HEAD 旧方法产物逐项一致（元素类型、顺序、数量）。
 */
public class EasyApiWebConfigurerParityTest {

    @Test
    public void testCacheDisabledConvertersMatchLegacy() {
        EasyApiWebConfigurer configurer = newConfigurer(false);
        List<HttpMessageConverter<?>> produced = defaultConverters(configurer);
        List<HttpMessageConverter<?>> legacy = legacyDefaultConverters();

        Assert.assertEquals(legacy.size(), produced.size());
        for (int i = 0; i < legacy.size(); i++) {
            Assert.assertEquals("第" + i + "个转换器类型不一致",
                    legacy.get(i).getClass(), produced.get(i).getClass());
        }
        // 关闭缓存时每次都重新构建，不应返回同一个列表实例
        List<HttpMessageConverter<?>> second = defaultConverters(configurer);
        Assert.assertNotSame(produced, second);
        AssertConverterTypes.equal(produced, second);
    }

    @Test
    public void testCacheEnabledConvertersStillMatchLegacy() {
        EasyApiWebConfigurer configurer = newConfigurer(true);
        List<HttpMessageConverter<?>> produced = defaultConverters(configurer);
        List<HttpMessageConverter<?>> legacy = legacyDefaultConverters();

        Assert.assertEquals(legacy.size(), produced.size());
        for (int i = 0; i < legacy.size(); i++) {
            Assert.assertEquals("第" + i + "个转换器类型不一致",
                    legacy.get(i).getClass(), produced.get(i).getClass());
        }
        Assert.assertSame(produced, defaultConverters(configurer));
    }

    private static EasyApiWebConfigurer newConfigurer(boolean cacheEnabled) {
        EasyApiWebConfigurer configurer = new EasyApiWebConfigurer();
        EasyApiConfig config = new EasyApiConfig();
        config.setEnableHttpMessageConverterCache(cacheEnabled);
        ReflectionTestUtils.setField(configurer, "easyApiConfig", config);
        return configurer;
    }

    @SuppressWarnings("unchecked")
    private static List<HttpMessageConverter<?>> defaultConverters(EasyApiWebConfigurer configurer) {
        return (List<HttpMessageConverter<?>>) ReflectionTestUtils.invokeMethod(
                configurer, "getDefaultHttpMessageConverter");
    }

    private static List<HttpMessageConverter<?>> legacyDefaultConverters() {
        return new WebMvcConfigurationSupport() {
            public List<HttpMessageConverter<?>> defaultMessageConverters() {
                return super.getMessageConverters();
            }
        }.defaultMessageConverters();
    }

    private static final class AssertConverterTypes {
        static void equal(List<HttpMessageConverter<?>> expected, List<HttpMessageConverter<?>> actual) {
            Assert.assertEquals(expected.size(), actual.size());
            List<String> expectedTypes = types(expected);
            List<String> actualTypes = types(actual);
            Assert.assertEquals(expectedTypes, actualTypes);
        }

        private static List<String> types(List<HttpMessageConverter<?>> converters) {
            List<String> types = new ArrayList<>();
            for (HttpMessageConverter<?> converter : converters) {
                types.add(converter.getClass().getName());
            }
            return types;
        }
    }
}
