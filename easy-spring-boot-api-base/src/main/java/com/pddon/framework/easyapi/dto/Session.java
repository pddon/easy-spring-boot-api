/**  
* Title Sesssion.java  
* Description  会话信息
* @author danyuan
* @date Oct 19, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.dto;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@Data
@Accessors(chain = true)
public class Session implements HttpSession, Serializable{
	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 应用ID，可以按应用维度控制api的访问权限
	 */
	private String appId;
	/**
	 * 渠道ID，如：安卓、iOS、微信、官网等等，可以按访问渠道控制api的访问权限
	 */
	private String channelId;
	
	/**
	 * 会话ID，用于便于用户认证后在一定时间段内进行有状态的访问后续接口
	 */
	private String sessionId;
	/**
	 * 用户访问唯一标识
	 */
	private String userId;
	/**
	 * 用户账号名
	 */
	private String username;
	/**
	 * 客户端ID,用于区分是哪个APP或者其他客户端的不同分支
	 */
	private String clientId;
	/**
	 * 客户端版本号
	 */
	private String versionCode;
	/**
	 * 客户端IP地址
	 */
	private String clientIp;
	
	/****************国际化相关的系统参数****************/	
	/**
	 * 语言，如： en_US、zh_CN等等
	 */
	private String locale;
	/**
	 * 货币类型,如：USD、CNY等等
	 */
	private String currency;
	/**
	 * 国家码，如： US、CN等等
	 */
	private String countryCode;
	/**
	 * 时区,如： GTM+8   北京时间所在时区
	 */
	private String timeZone;
	
	/**
	 * 其他可自定义配置的额外参数
	 */
	private Map<String, Object> expandParams = new HashMap<>();

	private Date createTime;

	private Date lastTime;

	private ServletContext servletContext;

	private HttpSessionContext sessionContext;

	private int maxInactiveInterval;

	private boolean newSession;

	private boolean recoverFromDB = false;

	private boolean isSuperManager;

	@JsonIgnore
	@Override
	public long getCreationTime() {
		return createTime.getTime();
	}

	@JsonIgnore
	@Override
	public String getId() {
		return sessionId;
	}

	@JsonIgnore
	@Override
	public long getLastAccessedTime() {
		return lastTime.getTime();
	}

	@JsonIgnore
	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@JsonIgnore
	@Override
	public HttpSessionContext getSessionContext() {
		return sessionContext;
	}

	@JsonIgnore
	@Override
	public Object getAttribute(String name) {
		return this.expandParams.get(name);
	}

	@JsonIgnore
	@Override
	public Object getValue(String name) {
		return this.expandParams.get(name);
	}

	@JsonIgnore
	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(this.expandParams.keySet());
	}

	@JsonIgnore
	@Override
	public String[] getValueNames() {
		Set<String> keys = this.expandParams.keySet();
		String[] stringArray = keys.toArray(new String[keys.size()]);
		return stringArray;
	}

	@JsonIgnore
	@Override
	public void setAttribute(String name, Object value) {
		this.expandParams.put(name, value);
	}

	@JsonIgnore
	@Override
	public void putValue(String name, Object value) {
		this.expandParams.put(name, value);
	}

	@JsonIgnore
	@Override
	public void removeAttribute(String name) {
		this.expandParams.remove(name);
	}

	@JsonIgnore
	@Override
	public void removeValue(String name) {
		this.expandParams.remove(name);
	}

	@JsonIgnore
	@Override
	public void invalidate() {

	}

	@JsonIgnore
	@Override
	public boolean isNew() {
		return newSession;
	}
}
