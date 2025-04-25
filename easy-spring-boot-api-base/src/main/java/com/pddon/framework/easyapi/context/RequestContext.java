/**  
* Title RequestContext.java
* Description  此对象用于存储一些用户会话相关的信息
* @author danyuan
* @date Dec 17, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.context;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.StringUtils;
import com.pddon.framework.easyapi.utils.UUIDGenerator;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;

import com.pddon.framework.easyapi.dto.ApiInfo;
import com.pddon.framework.easyapi.dto.ApiRestrictions;
import com.pddon.framework.easyapi.dto.Session;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class RequestContext {

    private final static String DATA_PERMISSION = "DATA_PERMISSION";
	 // *) 定义了ThreadLocal对象
    private static final ThreadLocal<RequestContext> LOCAL = new ThreadLocal<RequestContext>() {
        protected RequestContext initialValue() {
            return new RequestContext();
        }
    };
    
    private Class<?> targetClass;
    /**
     * 当前执行的方法信息
     */
    private Method method;
    /**
     * 当前执行的方法
     */
    private HandlerMethod handler;
    /**
     * 当前执行接口的请求
     */
    private Object request;
    /**
     * 当前执行接口的响应
     */
    private Object response;
    /**
     * API 限制条件
     */
    private ApiRestrictions apiRestrictions;
    /**
     * 当前接口信息
     */
    private ApiInfo apiInfo;
    /**
     * 当前会话信息
     */
    private Session session;
    /**
     * 是否忽略多租户条件
     */
    private boolean ignoreTenant = false;
    /**
     * 是否忽略数据权限
     */
    private boolean ignoreDataPerm = false;
    /**
     * 是否已启用shiro会话
     */
    private boolean shiroSessionEnable = false;
    /**
     * 是否是超级管理员
     */
    private boolean superManager = false;
    /**
	 * 对响应信息进行数字签名的结果
	 */
	private DefaultResponseWrapper<?> responseWrapper;

	/**
	 * 额外的系统响应参数
	 */
	private Map<String, Object> responseSystemParams = new HashMap<>();
	
    /**
     * 附带属性
     */
    private Map<String, Object> attachments = new HashMap<>();
 
    public static RequestContext getContext() {
        return (RequestContext)LOCAL.get();
    }
 
    protected RequestContext() {
    }
 
    public String getAttachment(String key) {
        return (String)this.attachments.get(key);
    }

    public Object getObjectAttachment(String key) {
        return this.attachments.get(key);
    }

    public RequestContext setAttachment(String key, String value) {
        return this.setObjectAttachment(key, value);
    }

    public RequestContext setObjectAttachment(String key, Object value) {
        if(value == null) {
            this.attachments.remove(key);
        } else {
            String requestIdKey = SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.REQUEST_ID);
            if(key.equalsIgnoreCase(requestIdKey)){
                MDC.put("requestId",value.toString());
            }
            this.attachments.put(key, value);
        } 
        return this;
    }
    
    public void clear() {
        this.attachments.clear(); 
        this.responseSystemParams.clear();
        this.targetClass = null;
        this.apiRestrictions = null;
        this.apiInfo = null;
        this.session = null;
        this.responseWrapper = null;
        this.method = null;
        this.handler = null;
        this.request = null;
        this.response = null;
        this.ignoreTenant = false;
        this.shiroSessionEnable = false;
        this.superManager = false;
    }

    public boolean isSuperManager() {
        return this.superManager || (this.session != null ? this.session.isSuperManager() : false);
    }
    
    public String getRequestId(){
        String key = SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.REQUEST_ID);
    	String requestId = this.getAttachment(key);
        if(StringUtils.isEmpty(requestId)){
            //生成调用链ID
            requestId = UUIDGenerator.getUUID();
            RequestContext.getContext().setAttachment(key, requestId);
        }
        return requestId;
    }

    public String getLocale(){
        return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.LOCALE));
    }
    
    public String getTimeZone(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.TIME_ZONE));
    }
    
    public String getSessionId(){
        if(session != null){
            return session.getSessionId();
        }
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SESSION_ID));
    }
    
    public String getUserId(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.USER_ID));
    }
    
    public String getAppId(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.APP_ID));
    }
    
    public String getChannelId(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.CHANNEL_ID));
    }
    
    public String getRepeatCode(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.REPEAT_CODE));
    }

    public String getSign(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SIGN));
    }
    
    public String getTimestamp(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.TIMESTAMP));
    }
    
    public String getVersionCode(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.VERSION_CODE));
    }
    
    public String getCountryCode(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.COUNTRY_CODE));
    } 
    
    public String getCurrency(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.CURRENCY));
    } 
    
    public String getClientId(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.CLIENT_ID));
    } 
    
    public String getClientIp(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.CLIENT_IP));
    } 
    
	public String getSecretId() {
		return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SECRET_ID));
	}
	
    /**
     * 设置响应系统参数信息
     * @author danyuan
     */
    public void setResponseSystemParamValue(String key, Object value){
    	responseSystemParams.put(key, value);
    }
    
    /**
     * 设置响应系统参数信息
     * @author danyuan
     */
    public Object getResponseSystemParamValue(String key){
    	return responseSystemParams.get(key);
    }

    public Map<String, Object> getDataPermissions(){
        Object data = this.getObjectAttachment(DATA_PERMISSION);
        if(data != null && data instanceof Map){
            return (Map<String, Object>)data;
        }
        return Collections.emptyMap();
    }

    public void setDataPermissions(Map<String, Object> perms){
        this.setObjectAttachment(DATA_PERMISSION, perms);
    }
	
}
