/**  
* Title AccountInfo.java  
* Description  
* @author danyuan
* @date Nov 14, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi.controller.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class AccountInfo implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	private String account;

	private BigDecimal amount;
	
	private BigDecimal freezeAmount;
	
	private String note;
}
