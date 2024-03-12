/**  
* Title BaseDeviceRequest.java  
* Description  
* @author danyuan
* @date Nov 6, 2018
* @version 1.0.0
* blog pddon.cn
*/ 
package com.pddon.framework.easyapi.client.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *@author danyuan
 *@date Nov 6, 2018
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
public class BaseRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//通过网关传递过来的系统参数
	private String appId;

	private String currency;

	private String countryCode;

	private String locale;

	private String timeZone;

	private String sessionId;

	private String sign;

	private Integer versionCode;

	private String mcc;

	private String imei;

	private String repeatCode;

	private Double longitude;

	private Double latitude;

	private Long timestamp;	
	
}
