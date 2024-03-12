/**  
* Title UniqueToken.java  
* Description  
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;
@Data
@Accessors(chain = true)
public class UniqueToken implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long crtTime;
	private boolean submit;
	
}
