/**  
* Title Sha1SignEncryptHandler.java  
* Description  
* @author danyuan
* @date Nov 14, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.encrypt.impl;

import com.pddon.framework.easyapi.encrypt.SignEncryptHandler;
import com.pddon.framework.easyapi.utils.EncryptUtils;

public class Sha1SignEncryptHandler implements SignEncryptHandler {

	/**
	 * @author danyuan
	 */
	@Override
	public String sign(String secrect, String body) {
		return EncryptUtils.encryptSHA1Hex(secrect, body);
	}

}
