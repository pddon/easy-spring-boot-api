/**  
* Title SignTestRequest.java  
* Description  
* @author danyuan
* @date Nov 14, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi.controller.request;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SignTestRequest implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	@NotEmpty
	private String name;
	private boolean sex;
	private Integer age;
	@NotNull
	private Map<String,String> tags;
	private List<String> likes;
}
