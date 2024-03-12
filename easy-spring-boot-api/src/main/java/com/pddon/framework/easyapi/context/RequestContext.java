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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import org.springframework.web.method.HandlerMethod;

import com.pddon.framework.easyapi.dto.ApiInfo;
import com.pddon.framework.easyapi.dto.ApiRestrictions;
import com.pddon.framework.easyapi.dto.Session;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class RequestContext {
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
    private HttpServletRequest request;
    /**
     * 当前执行接口的响应
     */
    private HttpServletResponse response;
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
	 * 对响应信息进行数字签名的结果
	 */
	private DefaultResponseWrapper<?> responseWrapper;

	/**
	 * 额外的系统响应参数
	 */
	private Map<String, Object> resonseSystemParams = new HashMap<>();
	
    /**
     * 附带属性
     */
    private final Map<String, Object> attachments = new HashMap<>();
 
    public static RequestContext getContext() {
        return (RequestContext)LOCAL.get();
    }
 
    protected RequestContext() {
    }
 
    public String getAttachment(String key) {
        return (String)this.attachments.get(key);
    }
 
    public RequestContext setAttachment(String key, String value) {
        if(value == null) {
            this.attachments.remove(key);
        } else {
            this.attachments.put(key, value);
        } 
        return this;
    }
    
    public void clear() {
        this.attachments.clear(); 
        this.resonseSystemParams.clear();
        this.targetClass = null;
        this.apiRestrictions = null;
        this.apiInfo = null;
        this.session = null;
        this.responseWrapper = null;
        this.method = null;
        this.handler = null;
        this.request = null;
        this.response = null;
    }
    
    public String getLocale(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.LOCALE));
    }
    
    public String getTimeZone(){
    	return this.getAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.TIME_ZONE));
    }
    
    public String getSessionId(){
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
    public void setResonseSystemParamValue(String key, Object value){
    	resonseSystemParams.put(key, value);
    }
    
    /**
     * 设置响应系统参数信息
     * @author danyuan
     */
    public Object getResonseSystemParamValue(String key){
    	return resonseSystemParams.get(key);
    }

	
}
