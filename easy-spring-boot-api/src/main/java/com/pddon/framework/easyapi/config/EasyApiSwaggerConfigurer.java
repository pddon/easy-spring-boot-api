/**  
* Title EasyApiSwaggerConfigurer.java
* Description  
* @author danyuan
* @date Nov 28, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.config;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.pddon.framework.easyapi.properties.EasyApiSwaggerProperties;
import com.pddon.framework.easyapi.properties.SystemParameterConfigProperties;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.swagger.OrderExtension;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.annotation.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.pddon.framework.easyapi.utils.ClassOriginCheckUtil;
import com.pddon.framework.easyapi.utils.StringUtils;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;

@EnableSwagger2
@EnableKnife4j
@EnableConfigurationProperties({ 
	EasyApiSwaggerProperties.class,
	SystemParameterConfigProperties.class
	})
@ConditionalOnProperty(name={"easyapi.enable","easyapi.enableSwagger"},havingValue = "true")
@ConditionalOnWebApplication
@AutoConfigureBefore(EasyApiWebConfigurer.class)
@Configuration
@Slf4j
public class EasyApiSwaggerConfigurer {

	@Bean
	public Docket easyApi(@Autowired SystemParameterRenameProperties systemParameterProperties, @Autowired SystemParameterConfigProperties otherParams) {
		Docket docket=new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(systemApiInfo())
				//分组名称
				.groupName("EasyApi内置API")
				.globalOperationParameters(getGlobalOperationParameters(systemParameterProperties, otherParams, true))
				.select()
				//这里指定Controller扫描包路径
				.apis(RequestHandlerSelectors.basePackage("com.pddon.framework.easyapi.controller"))
				.paths(PathSelectors.any())
				.build()
				.extensions(Lists.newArrayList(new OrderExtension(0)));//添加扩展
		return docket;
	}
	
	@Bean
	public Docket api(@Autowired SystemParameterConfigProperties otherParams, @Autowired EasyApiSwaggerProperties swagger, @Autowired SystemParameterRenameProperties systemParameterProperties) {
		String basePackage = swagger.getBasePackage();
		if(StringUtils.isBlank(basePackage)){
			basePackage = ClassOriginCheckUtil.getDeafultBasePackage();
		}
		if(log.isTraceEnabled()){
			log.trace("文档自动生成器开始扫描包[{}]内的所有API!", basePackage);
		}
		Docket docket=new Docket(DocumentationType.SWAGGER_2)
				        .apiInfo(apiInfo(swagger))
				        //分组名称
				        .groupName(swagger.getTitle())
				        .globalOperationParameters(getGlobalOperationParameters(systemParameterProperties, otherParams, false))
				        .select()
				        //这里指定Controller扫描包路径
				        .apis(RequestHandlerSelectors.basePackage(basePackage))
				        .paths(PathSelectors.any())
				        .build()
						.extensions(Lists.newArrayList(new OrderExtension(-1)));//添加扩展
		return docket;
	}
	
	private List<Parameter> getGlobalOperationParameters(SystemParameterRenameProperties systemParameterProperties, 
			SystemParameterConfigProperties otherParams, boolean exclude) {
		List<Parameter> pars = new ArrayList<>();
		
        ParameterBuilder parameterBuilder = new ParameterBuilder();
        if(!exclude){
        	otherParams.toParamMap().forEach((key,value) -> {
            	parameterBuilder.name(value).description(key)
            	.modelRef(new ModelRef("string"))
            	.parameterType("query")
            	.required(false);
            	pars.add(parameterBuilder.build());
            });        
        }
        SystemParameterRenameProperties.DEFAULT_PARAM_MAP.forEach((key,value) -> {
        	if(!(exclude && otherParams.getOtherParams() !=null && otherParams.getOtherParams().contains(key))){
        		parameterBuilder.name(value).description(key)
            	.modelRef(new ModelRef("string"))
            	.parameterType("query")
            	.required(false);
            	pars.add(parameterBuilder.build());
        	}        	
        });        
		return pars;
	}

	@SuppressWarnings("deprecation")
	private ApiInfo apiInfo(EasyApiSwaggerProperties swagger) {
		return new ApiInfoBuilder().title(swagger.getTitle())
				.description(swagger.getDescription())
				.termsOfServiceUrl(swagger.getTermsOfServiceUrl())
				.contact(new Contact(swagger.getTitle(), swagger.getTermsOfServiceUrl(), swagger.getContact())).version(swagger.getVersion())
				.build();
	}
	
	@SuppressWarnings("deprecation")
	private ApiInfo systemApiInfo() {
		return new ApiInfoBuilder().title("EasyApi内置API列表")
				.description("EasyApi默认提供的一些公共的通用API")
				.termsOfServiceUrl("https://github.com/pddon/easy-spring-boot-api")
				.contact(new Contact("EasyApi内置API列表", "https://pddon.cn", "service@pddon.com")).version("v1.0.0")
				.build();
	}

}
