/**  
* Title SwaggerProperties.java  
* Description  
* @author danyuan
* @date Mar 14, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.properties;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "easyapi.api.swagger")
public class EasyApiSwaggerProperties {
	private String basePackage;
	private String title;
	private String description;
	private String termsOfServiceUrl;
	private String contact;
	private String version;
	public EasyApiSwaggerProperties(){
		basePackage="";
		title="业务接口清单";
		description="接口文档随着接口动态同步更新！";
		termsOfServiceUrl="http://localhost:8080/doc.html";
		contact="后台开发同学";
		version="1.0";
	}
}
