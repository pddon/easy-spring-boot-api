/**  
* Title ClientSha1SignEncryptHandler.java
* Description  
* @author danyuan
* @date Nov 14, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.impl;

import com.pddon.framework.easyapi.client.ClientSignEncryptHandler;
import com.pddon.framework.easyapi.utils.EncryptUtils;

public class ClientSha1SignEncryptHandler implements ClientSignEncryptHandler {

	/**
	 * @author danyuan
	 */
	@Override
	public String sign(String secret, String body) {
		return EncryptUtils.encryptSHA1Hex(secret, body);
	}

}
