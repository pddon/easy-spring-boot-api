/**  
 * Title WebConfig.java  
 * Description  spring mvc 框架的一些配置信息
 * @author danyuan
 * @date Nov 7, 2018
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.config;


import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import com.pddon.framework.easyapi.filter.RepeatedlyReadFilter;
import com.pddon.framework.easyapi.interceptor.EasyApiHandlerInterceptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import com.pddon.framework.easyapi.properties.ChannelConfigProperties;
import com.pddon.framework.easyapi.properties.ErrorCodeProperties;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.properties.ResponseSystemFieldRenameProperties;
import com.pddon.framework.easyapi.properties.SystemParameterConfigProperties;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.ClassOriginCheckUtil;

@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Data
@Slf4j
@EnableConfigurationProperties({ EasyApiConfig.class,
	ChannelConfigProperties.class,
	ResponseSystemFieldRenameProperties.class,
	SystemParameterRenameProperties.class,
	SystemParameterConfigProperties.class,
	ErrorCodeProperties.class
	})
@ComponentScan("com.pddon.framework.easyapi")
@ConditionalOnProperty(name={"easyapi.enable"},havingValue = "true")
public class EasyApiWebConfigurer implements WebMvcConfigurer {

	@Autowired
	private EasyApiHandlerInterceptor easyApiHandlerInterceptor;

	@Override
	public void addFormatters(FormatterRegistry registry) {
		WebMvcConfigurer.super.addFormatters(registry);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(easyApiHandlerInterceptor).order(Ordered.HIGHEST_PRECEDENCE);
		if(log.isTraceEnabled()){
			log.trace("Add EasyApiHandlerInterceptor success !");
		}		
		WebMvcConfigurer.super.addInterceptors(registry);
	}	

	@Override
	public void configureMessageConverters(
			List<HttpMessageConverter<?>> converters) {
		// 添加默认转换器
		converters.addAll(getDefaultHttpMessageConverter());			
		converters.removeIf(httpMessageConverter -> httpMessageConverter.getClass() == StringHttpMessageConverter.class);
		
	}	
	
	private List<HttpMessageConverter<?>> getDefaultHttpMessageConverter(){
		return new WebMvcConfigurationSupport() {

			public List<HttpMessageConverter<?>> defaultMessageConverters() {
				return super.getMessageConverters();
			}

		}.defaultMessageConverters();
	}

	@Override
	public Validator getValidator() {
		return WebMvcConfigurer.super.getValidator();
	}
	
	@Bean
    public FilterRegistrationBean<RepeatedlyReadFilter> repeatedlyReadFilter() {
        FilterRegistrationBean<RepeatedlyReadFilter> registration = new FilterRegistrationBean<RepeatedlyReadFilter>();
        RepeatedlyReadFilter repeatedlyReadFilter = new RepeatedlyReadFilter();
        registration.setFilter(repeatedlyReadFilter);
        registration.addUrlPatterns("/*");
        return registration;
    }

	@Override
	public void addArgumentResolvers(
			List<HandlerMethodArgumentResolver> resolvers) {
		if(resolvers == null){
			resolvers = new ArrayList<>();
		}		
	    //对接口注解继承能力的支持，以便于feign和controller继承自相同的Interface接口
		// RequestBody 支持接口注解
		resolvers.add(new RequestResponseBodyMethodProcessor(getDefaultHttpMessageConverter()) {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return super.supportsParameter(ClassOriginCheckUtil.interfaceMethodParameter(parameter, RequestBody.class));
            }

            @Override
            // 支持@Valid验证
            protected void validateIfApplicable(WebDataBinder binder, MethodParameter methodParam) {
                super.validateIfApplicable(binder, ClassOriginCheckUtil.interfaceMethodParameter(methodParam, Valid.class));
            }
        });
		
		WebMvcConfigurer.super.addArgumentResolvers(resolvers);
	}


}
