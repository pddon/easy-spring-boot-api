/**  
* Title ComplextRequest.java  
* Description  
* @author danyuan
* @date Nov 14, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ComplextRequest implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	public String test="aaa";
	
	public boolean flag = true;

	private List<SkuItem> skuList;
	
	private Map<String, SkuItem> skuTagMap;
	
	private int totalSku;
	
	private String desc;
	
	private String[] introArr;
	
	public boolean isFlag(){
		return flag;
	}
	
	public String isTest(){
		return test;
	}
}
