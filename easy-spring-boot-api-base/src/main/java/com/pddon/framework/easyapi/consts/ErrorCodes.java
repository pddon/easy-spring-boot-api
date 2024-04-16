/**  
* Title ErrorCodes.java  
* Description  系统错误码定义
* @author danyuan
* @date Nov 6, 2018
* @version 1.0.0
* blog pddon.cn
*/ 
package com.pddon.framework.easyapi.consts;

import java.nio.ByteBuffer;

import com.pddon.framework.easyapi.utils.BeanPropertyUtil;

public enum ErrorCodes {
	//系统参数错误码
	SUCCESS(0),//成功
	SERVICE_DISABLE_ERROR(100001),//后端服务不可用	
	UNKOWN_ERROR(100000),//系统内部错误
	BUSINNES_ERROR(200000),//业务异常
	
	//接口状态相关错误码
	INVALID_MEDIA_TYPE(110001),//HTTP请求Content-Type类型错误
	INVALID_HTTP_ACTION(110002),//HTTP请求类型错误
	NOT_FOUND(110003),//无效的接口名
	TIMEOUT(110004),//请求超时
	NETWORK_ERROR(110005),//网络异常
	
	//参数校验相关错误码
	PARAM_FORMAT_ERROR(120001),//请求参数格式解析异常，请检查
	INVALID_PARAM(120002),//参数错误
	PARAM_LOST(120003),//参数缺失
	
	//系统业务错误码
	FORCEOUT_SESSION_ID(130001),//被踢出的会话ID
	INVALID_SESSION_ID(130002),//无效的会话ID
	NEED_SESSION_ID(130003),//需要会话ID（需要登录）
	ERROR_SIGN(130005),//验签失败
	UNIQUE_TOKEN_USING(130006),//您提交的请求正在处理，请勿重复提交
	UNIQUE_TOKEN_USED(130007),//你的请求数据已提交成功，请勿重复提交
	
	//接口权限相关错误码	
	NOT_PERMIT_APPID(140001),//该渠道禁止访问	
	INVALID_APPID(140002),//无效的渠道标识	
	EXCEED_USER_INVOKE_LIMITED(140003),//用户总调用次数超限
	EXCEED_SESSION_INVOKE_LIMITED(140004),//用户该次会话内调用次数超限
	EXCEED_APP_INVOKE_LIMITED(140005),//渠道总调用次数超限
	EXCEED_APP_INVOKE_FREQUENCY_LIMITED(140006),//渠道单位时间内调用频次超限
	
	//管理配置错误
	NOT_FOUND_CONFIG(150001)//未找到配置信息	
	;
	
	
	private Integer code;
	private String msgCode;
	private ErrorCodes(Integer code)
    {
        this.code = code;
        this.msgCode = "system."+BeanPropertyUtil.lineToHump(this.name());
    }
	public Integer getCode(){
		return this.code;
	}
	public String getMsgCode(){
		return this.msgCode;
	}
	public static ErrorCodes getByCode(Integer code){
		for(ErrorCodes error :ErrorCodes.values()){
			if(code.equals(error.getCode()))
				return error;
		}
		return ErrorCodes.BUSINNES_ERROR;
	}
	
	public static Boolean isSuccess(Integer code){
		return SUCCESS.getCode().equals(code);
	}
	
	public static byte[] getBytesOfCode(int code)
    {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(code);
        return buffer.array();
    }
}
