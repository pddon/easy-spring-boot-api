/**  
* Title SignManager.java  
* Description  渠道账号秘钥验签、鉴权管理器
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi;

import java.util.List;

import com.pddon.framework.easyapi.dto.ApiRequestParameter;

public interface SignManager {
		
	/**
	 * 生成数字签名
	 * @author danyuan
	 */
	String sign(String secret, String token, List<ApiRequestParameter> params);
	/**
	 * 验签操作
	 * @author danyuan
	 */
	boolean checkSign(String sign, String secret, String token, List<ApiRequestParameter> params);
}
