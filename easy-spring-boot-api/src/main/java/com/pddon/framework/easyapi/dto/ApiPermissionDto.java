/**  
* Title ApiPermissionDto.java  
* Description  
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import java.util.Set;

import lombok.Data;

@Data
public class ApiPermissionDto {
	/**
	 * 对称秘钥，用于加签、验签，对数据进行对称加解密
	 */
	private String secret;
	/**
	 * 非对称秘钥对，公钥加密，私钥解密
	 */
	private SecretKeyPair keyPair;
	/**
	 * 是否允许该渠道访问
	 */
	private Boolean enable;
	/**
	 * ip访问黑名单
	 */
	private Set<String> blackIpList;
	/**
	 * 应用访问黑名单
	 */
	private Set<String> blackAppIdList;
	/**
	 * 单位时间窗口内，单个用户访问的最大频次数
	 */
	private Long userMaxAccessCount;
	/**
	 * 单位时间窗口内，单个用户单个会话期间访问的最大频次数
	 */
	private Long userSessionMaxAccessCount;
	/**
	 * 单位时间窗口内，接口总访问的最大频次数
	 */
	private Long totalMaxAccessCount;
	/**
	 * 时间窗口，当前时间减去这个值所得到的初始值为统计区间,startTime = endTime - timeSection; endTime = currentTime
	 */
	private Long timeSection;
}
