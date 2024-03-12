/**  
 * Title PasswordGenerator.java  
 * Description  
 * @author danyuan
 * @date Sep 17, 2019
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.utils;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PasswordGenerator {

	public static String generate() {
		return generate(null);
	}

	/**
	 * 随机生成秘钥
	 */
	public static String generate(Integer len) {
		try {
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			if(len == null){
				len = 128;
			}
			kg.init(len);
			// 要生成多少位，只需要修改这里即可128, 192或256
			SecretKey sk = kg.generateKey();
			byte[] b = sk.getEncoded();
			String pass = byteToHexString(b);
			if(log.isDebugEnabled()){
				log.debug(pass);
			}		
			return pass;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			log.error("没有此算法:AES。");
		}
		return null;
	}

	/**
	 * byte数组转化为16进制字符串
	 * 
	 * @param bytes
	 * @return
	 */
	public static String byteToHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String strHex = Integer.toHexString(bytes[i]);
			if (strHex.length() > 3) {
				sb.append(strHex.substring(6));
			} else {
				if (strHex.length() < 2) {
					sb.append("0" + strHex);
				} else {
					sb.append(strHex);
				}
			}
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(generate());
	}

}
