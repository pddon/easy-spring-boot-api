/**  
* Title StringUtils.java  
* Description  
* @author danyuan
* @date Dec 2, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.utils;

import org.springframework.lang.Nullable;

public class StringUtils {

	public static boolean isEmpty(@Nullable Object str) {
		return (str == null || "".equals(str));
	}
	
	public static boolean isNotEmpty(@Nullable Object str) {
		return !isEmpty(str);
	}
	
	public static boolean isBlank(@Nullable Object str) {
		return (str == null || "".equals(str) || str.toString().trim().equals(""));
	}
	
	public static boolean isNotBlank(@Nullable Object str) {
		return !isBlank(str);
	}	
	
}
