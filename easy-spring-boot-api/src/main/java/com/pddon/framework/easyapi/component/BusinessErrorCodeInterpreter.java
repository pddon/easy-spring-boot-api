/**  
* Title BusinessErrorCodeInterpreter.java  
* Description  错误码工具包
* @author danyuan
* @date Nov 8, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import com.pddon.framework.easyapi.properties.ErrorCodeProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.dto.BusinessErrorDescription;
import com.pddon.framework.easyapi.dto.ErrorCodeDto;
import com.pddon.framework.easyapi.utils.HashUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BusinessErrorCodeInterpreter {
	private final static HashMap<String, Integer> codeMap = new HashMap<>();

	@Autowired
	private ErrorCodeProperties errorCodeProperties;
	
	/**
	 * 初始化错误码映射关系
	 * 
	 * @author danyuan
	 */
	@PostConstruct
	@DependsOn("errorCodeProperties")
	public void init(){
		codeMap.clear();
		if(errorCodeProperties.getBusinessErrorCodes() != null && errorCodeProperties.getBusinessErrorCodes().size() > 0){
			BusinessErrorDescription error = null;
			for(String name : errorCodeProperties.getBusinessErrorCodes().keySet()){
				error = errorCodeProperties.getBusinessErrorCodes().get(name);
				Integer code = 0;
				if(error.getCode() == null){
					code = HashUtil.limitELFHash(name, errorCodeProperties.getBusinessCodeStart(), errorCodeProperties.getBusinessCodeEnd());
				}else{//兼容老版错误码
					code = error.getCode();
					if(code < errorCodeProperties.getBusinessCodeStart() || code > errorCodeProperties.getBusinessCodeEnd()){
						log.warn("业务错误码配置错误，{}未在{}-{}范围内，请修正!", code, 
								errorCodeProperties.getBusinessCodeStart(), errorCodeProperties.getBusinessCodeEnd());
						code = ErrorCodes.BUSINNES_ERROR.getCode();
					}
				}			
				if(codeMap.containsValue(code)){
					//说明产生了冲突，打印冲突信息
					log.error("业务错误码HASH值产生了冲突，请更新错误码名字再启动应用，错误码冲突如下：");
					log.error("存在值： {} = {}", name, code);
					code = ErrorCodes.BUSINNES_ERROR.getCode();
				}
				codeMap.put(name, code);
			}
			
		}			
	}
	
	public static Integer getCode(String error){
		if(codeMap.containsKey(error)){
			return codeMap.get(error);
		}
		return ErrorCodes.BUSINNES_ERROR.getCode();
	}
	
	/**
	 * 获取业务错误码集合
	 * @return
	 * @author danyuan
	 */
	public List<ErrorCodeDto> getBussinessCodes(){
		List<ErrorCodeDto> list = new ArrayList<>();
		ErrorCodeDto dto = null;
		for(String key : codeMap.keySet()){
			dto = new ErrorCodeDto();
			dto.setError(key)
				.setMsg(key)
				.setCode(codeMap.get(key))
				.setHexCode("0x0" + Integer.toHexString(codeMap.get(key)).toUpperCase());
			
			list.add(dto);
		}
		return list;
	}
	
	/**
	 * 获取系统错误码集合
	 * @return
	 * @author danyuan
	 */
	public List<ErrorCodeDto> getSystemCodes(){
		List<ErrorCodeDto> list = new ArrayList<>();
		ErrorCodeDto dto = null;
		for(ErrorCodes error : ErrorCodes.values()){	
			dto = new ErrorCodeDto();
			dto.setError(error.getMsgCode())
				.setMsg(dto.getError());				
			if(errorCodeProperties.getSystemErrorCodes().containsKey(error.name())){
				//自定义系统错误码转换
				dto.setCode(errorCodeProperties.getSystemErrorCodes().get(error.name()))
					.setHexCode("0x0" + Integer.toHexString(errorCodeProperties.getSystemErrorCodes().get(error.name())).toUpperCase());
			}else{
				dto.setCode(error.getCode())
					.setHexCode("0x0" + Integer.toHexString(error.getCode()).toUpperCase());
			}
			
			list.add(dto);
		}
		return list;
	}
}
