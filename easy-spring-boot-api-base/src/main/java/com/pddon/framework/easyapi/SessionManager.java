/**  
* Title SessionManager.java  
* Description  会话管理器
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.dto.Session;

public interface SessionManager {
	/**
	 * 获取当前会话信息
	 * @author danyuan
	 */
	Session getCurrentSession(boolean createNew);
	/**
	 * 保存会话信息
	 * @author danyuan
	 */
	void persist(Session session);

	/**
	 * 更新会话信息
	 * @param session
	 */
	void update(Session session);
	/**
	 * 查询会话信息
	 * @author danyuan
	 */
	Session get(String sessionId);
	/**
	 * 检查会话信息是否存在
	 * @author danyuan
	 */
	boolean exists(String sessionId);
	/**
	 * 设置会话最大存活时间
	 * @author danyuan
	 */
	void setExpireSeconds(long seconds);
	/**
	 * 刷新会话存货时间
	 * @author danyuan
	 */
	void refreshAliveTime(String sessionId);
	/**
	 * 删除会话信息
	 * @author danyuan
	 */
	void remove(String sessionId);
}
