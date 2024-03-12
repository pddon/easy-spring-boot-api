/**  
* Title DenyRepeatSubmitType.java  
* Description  
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.consts;

public enum DenyRepeatSubmitType {
	/**
	 * 客户端主动生成全局唯一防重复提交码模式
	 */
	GENERATE_TOKEN,
	/**
	 * 使用数字签名作为防重码模式
	 */
	USE_SIGN
}
