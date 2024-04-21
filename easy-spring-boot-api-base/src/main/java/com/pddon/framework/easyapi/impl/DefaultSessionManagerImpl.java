/**  
* Title DefaultSessionManagerImpl.java  
* Description  
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.UUIDGenerator;
import org.springframework.util.StringUtils;

import lombok.Setter;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.SessionManager;
import com.pddon.framework.easyapi.dto.Session;

import java.util.Date;

@Setter
public class DefaultSessionManagerImpl implements SessionManager {

	protected CacheManager cacheManager;
	
	protected long expireSeconds = 15;
	
	/**
	 * @author danyuan
	 */
	@Override
	public Session getCurrentSession(boolean createNew) {
		Session session = RequestContext.getContext().getSession();
		String sessionId = RequestContext.getContext().getSessionId();
		
		if(!StringUtils.isEmpty(sessionId)){
			session = this.get(sessionId);
		}
		if(session == null && createNew){
			//创建会话
			sessionId = UUIDGenerator.getUUID();
			session = new Session();
			RequestContext context = RequestContext.getContext();
			session.setSessionId(sessionId)
				.setAppId(context.getAppId())
				.setChannelId(context.getChannelId())
				.setClientId(context.getClientId())
				.setClientIp(context.getClientIp())
				.setCountryCode(context.getCountryCode())
				.setCurrency(context.getCurrency())
				.setLocale(context.getLocale())
				.setTimeZone(context.getTimeZone())
				.setUserId(context.getUserId())
				.setVersionCode(context.getVersionCode())
				.setNewSession(true)
				.setCreateTime(new Date())
				;
			
			cacheManager.set(sessionId, session, expireSeconds, CacheExpireMode.EXPIRE_AFTER_REDA);
			RequestContext.getContext().setSession(session);
			RequestContext.getContext().setAttachment(
					SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SESSION_ID), sessionId);
		}else{
			session.setNewSession(false).setLastTime(new Date());
		}
		return session;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void persist(Session session) {
		//创建会话
		String sessionId = UUIDGenerator.getUUID();
		session.setSessionId(sessionId);
		cacheManager.set(sessionId, session, expireSeconds, CacheExpireMode.EXPIRE_AFTER_REDA);
		RequestContext.getContext().setSession(session);
		RequestContext.getContext().setAttachment(
				SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SESSION_ID), sessionId);
	}

	@Override
	public void update(Session session) {
		cacheManager.set(session.getSessionId(), session, expireSeconds, CacheExpireMode.EXPIRE_AFTER_REDA);
		RequestContext.getContext().setSession(session);
		RequestContext.getContext().setAttachment(
				SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SESSION_ID), session.getSessionId());
	}

	/**
	 * @author danyuan
	 */
	@Override
	public Session get(String sessionId) {
		Session session = cacheManager.get(sessionId, Session.class, this.expireSeconds, CacheExpireMode.EXPIRE_AFTER_REDA);
		return session;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public boolean exists(String sessionId) {
		return cacheManager.exists(sessionId, this.expireSeconds, CacheExpireMode.EXPIRE_AFTER_REDA);
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void setExpireSeconds(long seconds) {
		this.expireSeconds = seconds;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void refreshAliveTime(String sessionId) {
		cacheManager.setExpire(sessionId, expireSeconds, expireSeconds, CacheExpireMode.EXPIRE_AFTER_REDA);
	}

	/**
	 * @author danyuan
	 */
	@Override
	public void remove(String sessionId) {
		cacheManager.remove(sessionId, this.expireSeconds, CacheExpireMode.EXPIRE_AFTER_REDA);
		RequestContext.getContext().setSession(null);
	}

}
