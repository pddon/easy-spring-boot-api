package com.pddon.framework.easyapi.utils;

import java.util.Random;

public class RandomTokenGenerator {
	public static final int DEAULT_CHARCOUNT=6;
	/**
	 * 默认生成6位随机数字
	 * @return
	 */
	public static String generateToken(){
		return generateToken(DEAULT_CHARCOUNT);
	}
	
	public static String generateToken(int charCount){
		 StringBuffer buffer = new StringBuffer();
	        for (int i = 0; i < charCount; i++) {
	            char c = (char) (randomInt(0, 10) + '0');
	            buffer.append(String.valueOf(c));
	        }
	        return buffer.toString();
	}
	
	public static int randomInt(int from, int to) {
        Random r = new Random();
        return from + r.nextInt(to - from);
    }
}
