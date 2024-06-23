package com.pddon.framework.easyapi.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
public class EncryptUtils {

	private static final Base64.Decoder base64Decoder = Base64.getDecoder();
	private static final Base64.Encoder base64Encoder = Base64.getEncoder();

	/**
	 * 排序并拼装字符串
	 * 
	 * @param paramValues
	 * @return
	 */
	public static String sortAndMontage(Map<String, String> paramValues) {
		try {
			StringBuilder sb = new StringBuilder();
			List<String> paramNames = new ArrayList<String>(paramValues.size());
			paramNames.addAll(paramValues.keySet());

			Collections.sort(paramNames);
			for (String paramName : paramNames) {
				// 对键值对进行拼接
				sb.append(paramName).append(paramValues.get(paramName));
			}			
			return sb.toString();
		} catch (Exception e) {
			log.error(IOUtils.getThrowableInfo(e));
			return null;
		}
	}

	public static String encryptSHA1Hex(String key, String body) {
		String text = key + body + key;
		return encryptSHA1Hex(text);
	}

	public static String encryptSHA1Hex(String content) {
		String outStr = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digest = md.digest(content.getBytes());
			outStr = Hex.encodeHexString(digest).toUpperCase();
			if (log.isTraceEnabled()) {
				log.trace("sign:" + outStr);
			}
		} catch (NoSuchAlgorithmException e) {
			log.error(IOUtils.getThrowableInfo(e));
		} catch (Exception e) {
			log.error(IOUtils.getThrowableInfo(e));
		}
		return outStr;
	}

	public static String encryptMD5Hex(String key, String body) {
		String data = key;
		if (body != null) {
			data += body;
		}
		return encryptMD5Hex(data);
	}

	public static String encryptMD5Hex(String content) {
		String outStr = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] md5Byte = digest.digest(content.getBytes());
			outStr = Hex.encodeHexString(md5Byte).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			log.error(IOUtils.getThrowableInfo(e));
		}
		return outStr;
	}

	/**
	 * 字符串转换为16进制字符串
	 * 
	 * @param s
	 * @return
	 */
	public static String stringToHexString(String s) {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			if (s4.length() < 2) {
				str.append('0');
			}
			str.append(s4);
		}
		return str.toString().toUpperCase();
	}

	/**
	 * 使用128 位 AES加密算法进行加密
	 * @author danyuan
	 */
	public static String encodeAES128(String password, String content)
			throws Exception {
		// 1.构造密钥生成器，指定为AES算法,不区分大小写
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		/*// 2.生成一个128位的随机源
		keygen.init(128, new SecureRandom(password.getBytes("UTF-8")));
		// 3.产生原始对称密钥
		SecretKey original_key = keygen.generateKey();
		// 4.获得原始对称密钥的字节数组
		byte[] raw = original_key.getEncoded();
		// 5.根据字节数组生成AES密钥
		SecretKey key = new SecretKeySpec(raw, "AES");*/
		SecretKey key = new SecretKeySpec(password.getBytes("UTF-8"), "AES");
		// 6.根据指定算法AES自成密码器
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		// 7.初始化密码器，选择加解密模式
		cipher.init(Cipher.ENCRYPT_MODE, key);
		// 8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
		byte[] byte_encode = content.getBytes("UTF-8");
		byte[] byte_AES = cipher.doFinal(byte_encode);
		String AES_encode = new String(base64Encoder.encode(byte_AES));
		return AES_encode;
	}

	/**
	 * 使用128 位 AES解密算法进行解密
	 * @author danyuan
	 */
	public static String decodeAES128(String password, String content)
			throws Exception {
		// 1.构造密钥生成器，指定为AES算法,不区分大小写
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		/*// 2.生成一个128位的随机源,根据传入的字节数组
		keygen.init(128, new SecureRandom(password.getBytes("UTF-8")));
		SecretKey original_key = keygen.generateKey();
		byte[] raw = original_key.getEncoded();
		SecretKey key = new SecretKeySpec(raw, "AES");*/
		SecretKey key = new SecretKeySpec(password.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] byte_content = base64Decoder.decode(content);
		byte[] byte_decode = cipher.doFinal(byte_content);
		String AES_decode = new String(byte_decode, "UTF-8");
		return AES_decode;
	}

	public static void main(String[] args) {
		String key = "HJKNGGHwEF3TG5TR";
		String body = "公钥20240616";

		System.out.println(EncryptUtils.encryptSHA1Hex(key, body));

		System.out.println(EncryptUtils.encryptMD5Hex("123456", null));
		
		try {
			String encryptContent = EncryptUtils.encodeAES128(key, body);
			System.out.println("encodeAES128:"+encryptContent);
			System.out.println("decodeAES128:"+EncryptUtils.decodeAES128(key, encryptContent));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
