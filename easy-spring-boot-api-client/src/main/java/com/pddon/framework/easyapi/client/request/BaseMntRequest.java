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
public class BaseMntRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String userId;	

	private String countryCode;

	private String locale;

	private String timeZone;

	private String sessionId;

	private String sign;

	private String repeatCode;

	private Long timestamp;	
	
}
