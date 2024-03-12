/**  
* Title DefaultInvokeTimesManagerImpl.java  
* Description  
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.ApplicationManager;
import com.pddon.framework.easyapi.InvokeApiLogManager;
import com.pddon.framework.easyapi.InvokeTimesManager;

public class DefaultInvokeTimesManagerImpl implements InvokeTimesManager {

	/**
	 * @author danyuan
	 */
	@Override
	public void setInvokeApiLogManager(InvokeApiLogManager invokeApiLogManager) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void setApplicationManager(ApplicationManager applicationManager) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void checkClientIpInvokeTimes(String clientIp) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void checkAppTotalInvokeTimes(String appId) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void checkAppUserTotalInvokeTimes(String appId, String userId) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void checkAppUserSessionTotalInvokeTimes(String appId,
			String sessionId) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void checkAppInvokeTimes(String appId, String apiName) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void checkAppUserInvokeTimes(String appId, String userId,
			String apiName) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void checkAppUserSessionInvokeTimes(String appId, String sessionId,
			String apiName) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void checkAppTimeSectionInvokeTimes(String appId, String apiName) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void checkAppUserTimeSectionInvokeTimes(String appId, String userId,
			String apiName) {
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void checkClientIpTimeSectionInvokeTimes(String clientIp) {
	}

}
