/**  
* Title SignEncryptHandler.java  
* Description  
* @author danyuan
* @date Nov 14, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.encrypt;

public interface SignEncryptHandler {
	
	String sign(String secret, String body);
}
