/**  
* Title InvokeTimesManager.java  
* Description  接口调用频次控制
* @author danyuan
* @date Nov 9, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi;


public interface InvokeTimesManager {
	/**
	 * 注入接口日志管理器
	 * @author danyuan
	 */
	void setInvokeApiLogManager(InvokeApiLogManager invokeApiLogManager);
	/**
	 * 注入应用权限信息管理器
	 * @author danyuan
	 */
	void setApplicationManager(ApplicationManager applicationManager);
	/**
	 * 校验某客户端IP对所有接口的总调用次数是否超限
	 * @author danyuan
	 */
	void checkClientIpInvokeTimes(String clientIp);
	/**
	 * 校验应用的客户端对所有接口的总调用次数是否超限
	 * @author danyuan
	 */
	void checkAppTotalInvokeTimes(String appId);
	/**
	 * 校验应用的客户端对所有接口的总调用次数是否超限
	 * @author danyuan
	 */
	void checkAppUserTotalInvokeTimes(String appId, String userId);
	/**
	 * 校验应用内某用户某次会话内对所有接口的调用次数是否超限
	 * @author danyuan
	 */
	void checkAppUserSessionTotalInvokeTimes(String appId, String sessionId);
	/**
	 * 校验应用的客户端对某接口的总调用次数是否超限
	 * @author danyuan
	 */
	void checkAppInvokeTimes(String appId, String apiName);
	
	/**
	 * 校验应用内某用户对某接口的总调用次数是否超限
	 * @author danyuan
	 */
	void checkAppUserInvokeTimes(String appId, String userId, String apiName);
	/**
	 * 校验应用内某用户某次会话内对某接口的调用次数是否超限
	 * @author danyuan
	 */
	void checkAppUserSessionInvokeTimes(String appId, String sessionId, String apiName);
	/**
	 * 校验应用当前一段时间窗口内对某接口的调用次数是否超限
	 * @author danyuan
	 */
	void checkAppTimeSectionInvokeTimes(String appId, String apiName);
	
	/**
	 * 校验应用内某用户当前一段时间窗口内对某接口的调用次数是否超限
	 * @author danyuan
	 */
	void checkAppUserTimeSectionInvokeTimes(String appId, String userId, String apiName);
	
	/**
	 * 校验某客户端IP最近一段时间内对所有接口的总调用次数是否超限
	 * @author danyuan
	 */
	void checkClientIpTimeSectionInvokeTimes(String clientIp);
}
