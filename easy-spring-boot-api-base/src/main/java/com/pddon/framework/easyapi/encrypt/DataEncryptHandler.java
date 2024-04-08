/**  
* Title DataEncryptHandler.java  
* Description  数据加解密处理器
* @author danyuan
* @date Nov 15, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.encrypt;

public interface DataEncryptHandler {
	String encrypt(String appId, String channelId, String userId, String content) throws Throwable ;
	String decrypt(String appId, String channelId, String userId, String content) throws Throwable ;
}
