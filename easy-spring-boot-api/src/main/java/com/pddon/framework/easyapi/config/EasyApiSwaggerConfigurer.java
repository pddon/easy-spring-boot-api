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

import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import com.google.common.collect.Lists;
import com.pddon.framework.easyapi.properties.EasyApiSwaggerProperties;
import com.pddon.framework.easyapi.properties.ResponseSystemFieldRenameProperties;
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

import org.springframework.http.HttpMethod;
import springfox.documentation.builders.*;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.*;
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
	public Docket easyApi(@Autowired OpenApiExtensionResolver openApiExtensionResolver, @Autowired SystemParameterRenameProperties systemParameterProperties, @Autowired SystemParameterConfigProperties otherParams, @Autowired ResponseSystemFieldRenameProperties responseSystemFieldRenameProperties) {
		List<VendorExtension> extensions = openApiExtensionResolver.buildExtensions("使用说明文档");
		extensions.add(new OrderExtension(0));
		Docket docket=new Docket(DocumentationType.OAS_30)
				.apiInfo(systemApiInfo())
				//分组名称
				.groupName("EasyApi使用说明")
				.globalRequestParameters(getGlobalOperationParameters(systemParameterProperties, otherParams, false))
				.select()
				//这里指定Controller扫描包路径
				.apis(RequestHandlerSelectors.basePackage("com.pddon.framework.easyapi.controller"))
				.paths(PathSelectors.any())
				.build()
				.extensions(extensions);//添加扩展
				/*.globalResponses(HttpMethod.POST, getGlobalResponseMessage(responseSystemFieldRenameProperties))
				.globalResponses(HttpMethod.GET, getGlobalResponseMessage(responseSystemFieldRenameProperties))
				.globalResponses(HttpMethod.HEAD, getGlobalResponseMessage(responseSystemFieldRenameProperties))
				.globalResponses(HttpMethod.PUT, getGlobalResponseMessage(responseSystemFieldRenameProperties))
				.globalResponses(HttpMethod.PATCH, getGlobalResponseMessage(responseSystemFieldRenameProperties))
				.globalResponses(HttpMethod.DELETE, getGlobalResponseMessage(responseSystemFieldRenameProperties))*/
		return docket;
	}
	
	@Bean
	public Docket api(@Autowired OpenApiExtensionResolver openApiExtensionResolver, @Autowired SystemParameterConfigProperties otherParams, @Autowired EasyApiSwaggerProperties swagger, @Autowired SystemParameterRenameProperties systemParameterProperties) {
		String basePackage = swagger.getBasePackage();
		if(StringUtils.isBlank(basePackage)){
			basePackage = ClassOriginCheckUtil.getDeafultBasePackage();
		}
		if(log.isTraceEnabled()){
			log.trace("文档自动生成器开始扫描包[{}]内的所有API!", basePackage);
		}
		List<VendorExtension> extensions = openApiExtensionResolver.buildExtensions("使用说明文档");
		extensions.add(new OrderExtension(-1));
		Docket docket=new Docket(DocumentationType.OAS_30)
				        .apiInfo(apiInfo(swagger))
				        //分组名称
				        .groupName(swagger.getTitle())
				        //.globalRequestParameters(getGlobalOperationParameters(systemParameterProperties, otherParams, true))
				        .select()
				        //这里指定Controller扫描包路径
				        .apis(RequestHandlerSelectors.basePackage(basePackage))
				        .paths(PathSelectors.any())
				        .build()
						.extensions(extensions);//添加扩展
		return docket;
	}
	
	private List<RequestParameter> getGlobalOperationParameters(SystemParameterRenameProperties systemParameterProperties,
																SystemParameterConfigProperties otherParams, boolean exclude) {
		List<RequestParameter> pars = new ArrayList<>();
		
        RequestParameterBuilder parameterBuilder = new RequestParameterBuilder();
        if(!exclude){
        	otherParams.toParamMap().forEach((key,value) -> {
            	parameterBuilder.name(value).description(key)
						.in(ParameterType.QUERY)
						.query(q -> q.model(m -> m.scalarModel(ScalarType.STRING)))
						.required(false);
            	pars.add(parameterBuilder.build());
            });        
        }
        SystemParameterRenameProperties.DEFAULT_PARAM_MAP.forEach((key,value) -> {
        	if(!(exclude && otherParams.getOtherParams() !=null && otherParams.getOtherParams().contains(key))){
        		parameterBuilder.name(value).description(key)
						.in(ParameterType.QUERY)
						.query(q -> q.model(m -> m.scalarModel(ScalarType.STRING)))
						.required(false);
            	pars.add(parameterBuilder.build());
        	}        	
        });        
		return pars;
	}

	//生成通用响应状态码信息
	private List<Response> getGlobalResponseMessage(ResponseSystemFieldRenameProperties responseSystemFieldRenameProperties) {
		List<Response> responseList = new ArrayList<>();
		responseList.add(new ResponseBuilder().code(responseSystemFieldRenameProperties.getCode()).description("错误码，0成功，否则失败").build());
		responseList.add(new ResponseBuilder().code(responseSystemFieldRenameProperties.getSubCode()).description("业务错误码").build());
		responseList.add(new ResponseBuilder().code(responseSystemFieldRenameProperties.getMsg()).description("错误提示信息，当接口失败时必填").build());
		responseList.add(new ResponseBuilder().code(responseSystemFieldRenameProperties.getData()).description("接口响应数据信息内容").build());
		responseList.add(new ResponseBuilder().code(responseSystemFieldRenameProperties.getSign()).description("响应数据数字签名").build());
		responseList.add(new ResponseBuilder().code(responseSystemFieldRenameProperties.getTimestamp()).description("时间戳，用于数字签名和校验响应数据有效期").build());
		responseList.add(new ResponseBuilder().code("otherParams").description("预留出来的可拓展系统参数字段").build());
		return responseList;
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
				.description("EasyApi默认提供的一些公共的通用API和接口使用说明，比如接口加签/验签算法、加解密算法等等")
				.termsOfServiceUrl("https://github.com/pddon/easy-spring-boot-api")
				.contact(new Contact("pddon.cn", "https://pddon.cn", "service@pddon.com")).version("v1.0.0")
				.build();
	}

}
