/**  
* Title HashUtil.java  
* Description  
* @author danyuan
* @date Nov 8, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.utils;

public class HashUtil {
	
	public static int ELFhash(String str)//思想就是一直杂糅，使字符之间互相影响
	{
	    int h = 0x0, g;
	    for(int i = 0 ; i < str.length() ; i++)
	    {
	        h = (h<<4) + str.charAt(i); //h左移4位，当前字符占8位，加到h中进行杂糅
	        if((g = h & 0xf0000000) != 0) //取h最左四位的值，若均为0，则括号中执行与否没区别，故不执行
	        {
	            h ^= g>>24; //用h的最左四位的值对h的右起5~8进行杂糅
	            h &= ~g;//清空h的最左四位
	        }
	    }
	    return h; //因为每次都清空了最左四位，最后结果最多也就是28位二进制整数，不会超int
	}
	
	public static int limitELFHash(String str , int min, int max)
	{
	    int k = ELFhash(str);
	    k = Math.abs(k - min);
	    int result = k % (max - min);	
	    result += min;	    
	    return result;
	}
}
