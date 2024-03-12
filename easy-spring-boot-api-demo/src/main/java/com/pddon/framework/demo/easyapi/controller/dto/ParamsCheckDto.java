/**  
* Title ParamsCheckRequest.java  
* Description  
* @author danyuan
* @date Nov 29, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi.controller.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import lombok.Data;

@Data
@ApiModel
public class ParamsCheckDto implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	@NotBlank
	@Size(max=10, min=10, message = "账号长度只能为10!")
	@ApiModelProperty(value = "用户账号", required = true, example = "1321122321")
	private String account;
	
	@Email
	@ApiModelProperty(value = "用户邮箱号")
	private String email;
	
	@NotNull
	@Range(max=200, min=1, message = "年龄只能在1-200岁之间!")
	@ApiModelProperty(value = "用户年龄", required = true)
	private Integer age;
	
	@Size(max=3, min=0, message = "最多只能选择3个爱好!")
	@ApiModelProperty(value = "用户爱好")
	private List<String> likes;//爱好
}
