/**  
* Title ErrorCodeProperties.java  
* Description  
* @author danyuan
* @date Nov 28, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.properties;

import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.pddon.framework.easyapi.dto.BusinessErrorDescription;

@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "easyapi.error")
public class ErrorCodeProperties {

	
	/**
	 * 自定义系统错误码值
	 */
	private Map<String,Integer> systemErrorCodes;
	/**
	 * 业务错误码默认起始值
	 */
	private Integer businessCodeStart = 200001;
	/**
	 * 业务错误码默认最大值
	 */
	private Integer businessCodeEnd = 999999;
	/**
	 * 业务异常错误码值
	 */
	private Map<String, BusinessErrorDescription> businessErrorCodes;
	
}
