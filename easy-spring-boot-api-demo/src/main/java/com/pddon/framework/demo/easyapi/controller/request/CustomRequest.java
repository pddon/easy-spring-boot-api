/**  
* Title CustomRequest.java  
* Description  
* @author danyuan
* @date Dec 2, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi.controller.request;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CustomRequest implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	private String clientId;
	@NotBlank
	private String locale;
	
	private String appName;
	@NotBlank
	private String countryCode;
}
