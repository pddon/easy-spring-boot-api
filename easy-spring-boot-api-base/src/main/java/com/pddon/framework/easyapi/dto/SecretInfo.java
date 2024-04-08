/**  
* Title SecretInfo.java  
* Description  
* @author danyuan
* @date Dec 15, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SecretInfo implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 对称秘钥，用于加签、验签，对数据进行对称加解密
	 */
	private String secret;
	/**
	 * 非对称秘钥对，公钥加密，私钥解密
	 */
	private SecretKeyPair keyPair;
}
