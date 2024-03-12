/**  
* Title DefaultInvokeApiLogManagerImpl.java  
* Description  
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.impl;

import java.sql.Date;

import com.pddon.framework.easyapi.InvokeApiLogManager;
import com.pddon.framework.easyapi.dto.ApiInvokeLog;

public class DefaultInvokeApiLogManagerImpl implements InvokeApiLogManager {

	/**
	 * @author danyuan
	 */
	@Override
	public void save(ApiInvokeLog log) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public long count(String apiName, String appId, String userId,
			String sessionId, String clientIp, Date startTime, Date endTime) {
		return 0;
	}

}
