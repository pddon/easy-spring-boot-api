/**  
* Title UserInfoDto.java  
* Description  
* @author danyuan
* @date Nov 29, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi.controller.dto;

import java.io.Serializable;
import java.util.List;

import com.pddon.framework.easyapi.annotation.Encrypt;
import com.pddon.framework.easyapi.annotation.IgnoreSign;

import lombok.Data;

@Data
public class UserInfoDto implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String username;
	@IgnoreSign
	private Integer age;
	@Encrypt
	private String desc;
	
	private List<UserInfoDto> friends;
}
