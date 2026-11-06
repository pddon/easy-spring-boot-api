package com.pddon.framework.easyapi.config;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;

import com.pddon.framework.easyapi.properties.EasyApiConfig;

/**
 * 验证启动期消息转换器缓存开关：
 * 默认false时保持原有行为（每次构建新列表）；
 * 开启后复用同一份列表，避免configureMessageConverters与addArgumentResolvers重复构建。
 */
public class EasyApiWebConfigurerMessageConverterCacheTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testCacheDisabledKeepsOldBehavior() {
        EasyApiWebConfigurer configurer = new EasyApiWebConfigurer();
        EasyApiConfig config = new EasyApiConfig();
        config.setEnableHttpMessageConverterCache(false);
        ReflectionTestUtils.setField(configurer, "easyApiConfig", config);

        List<HttpMessageConverter<?>> first = invokeBuild(configurer);
        List<HttpMessageConverter<?>> second = invokeBuild(configurer);
        Assert.assertNotNull(first);
        Assert.assertFalse(first.isEmpty());
        Assert.assertNotSame("关闭缓存时应每次重新构建", first, second);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCacheEnabledReusesConverters() {
        EasyApiWebConfigurer configurer = new EasyApiWebConfigurer();
        EasyApiConfig config = new EasyApiConfig();
        config.setEnableHttpMessageConverterCache(true);
        ReflectionTestUtils.setField(configurer, "easyApiConfig", config);

        List<HttpMessageConverter<?>> first = invokeBuild(configurer);
        List<HttpMessageConverter<?>> second = invokeBuild(configurer);
        Assert.assertNotNull(first);
        Assert.assertFalse(first.isEmpty());
        Assert.assertSame("开启缓存后应复用同一份列表", first, second);
    }

    @SuppressWarnings("unchecked")
    private List<HttpMessageConverter<?>> invokeBuild(EasyApiWebConfigurer configurer) {
        return ReflectionTestUtils.invokeMethod(configurer, "getDefaultHttpMessageConverter");
    }
}
