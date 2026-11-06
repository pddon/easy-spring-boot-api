/**  
* Title AutoAddReponseWrapperHandler.java  
* Description  响应内容检查、包装处理拦截器
* @author danyuan
* @date Oct 18, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.response.enhance;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.properties.ResponseSystemFieldRenameProperties;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.utils.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class AutoAddResponseWrapperHandler extends AbstractResponseEnhanceHandler {

	@Autowired
	private ResponseSystemFieldRenameProperties responseWrapperProperties;
	
	@Autowired
	private EasyApiConfig easyApiConfig;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * 响应类型 -> 该类型声明的字段名集合，避免每次响应都反射扫描字段。
	 * 使用ClassValue按类隔离，类卸载时缓存随之释放
	 */
	private static final ClassValue<Set<String>> DECLARED_FIELD_NAMES_CACHE = new ClassValue<Set<String>>() {
		@Override
		protected Set<String> computeValue(Class<?> type) {
			Set<String> names = new HashSet<>();
			for(Field field : type.getDeclaredFields()){
				names.add(field.getName());
			}
			return Collections.unmodifiableSet(names);
		}
	};
	
	/**
	 * @author danyuan
	 */
	@Override
	public Object enhance(Object resp) {
		if(!easyApiConfig.getForceAutoAddResponseWrapper()){
			if(log.isTraceEnabled()){
				log.trace("跳过响应包装壳处理流程！");
			}
			return resp;
		}
		if(log.isTraceEnabled()){
			log.trace("开始检查和修正响应包装内容！");
		}
		Map<String, Object> map = null;
		try{
			if(RequestContext.getContext().getApiRestrictions() != null 
					&& !RequestContext.getContext().getApiRestrictions().getIgnoreResponseWrapper()){
				if(resp != null){			
			    	//检查是否是包装类
					if(hasResponseWrapper(resp)){
						if(resp instanceof DefaultResponseWrapper){
							DefaultResponseWrapper<?> wrapper = (DefaultResponseWrapper<?>)resp;
							map = new HashMap<>();
							map.put(responseWrapperProperties.getCode(), wrapper.getCode());
							map.put(responseWrapperProperties.getSubCode(), wrapper.getSubCode());
							map.put(responseWrapperProperties.getMsg(), wrapper.getMsg());
							map.put(responseWrapperProperties.getData(), wrapper.getData());
							if(RequestContext.getContext().getResponseWrapper() != null){//系统签名为
								map.put(responseWrapperProperties.getSign(), RequestContext.getContext().getResponseWrapper().getSign());
								map.put(responseWrapperProperties.getTimestamp(), RequestContext.getContext().getResponseWrapper().getTimestamp());
							}
							if(wrapper.getSign() != null){
								map.put(responseWrapperProperties.getSign(), wrapper.getSign());
							}
							if(wrapper.getTimestamp() != null){
								map.put(responseWrapperProperties.getTimestamp(), wrapper.getTimestamp());		
							}							
						} else {
							//说明存在响应包装
				    		map= baseResponseWrapper(resp);
						}	    			    			
			    	}else{
			    		map = successResponseWrapper();
			    		map.put(responseWrapperProperties.getData(), resp);
			    	}
				}else{//默认返回空的成功响应
					map = successResponseWrapper();
				}
			}	
		}catch(Exception e){
			if(log.isTraceEnabled()){
				log.trace(IOUtils.getThrowableInfo(e));
			}
		}	
		//去除空字段
		if(map != null){
			//添加其他系统响应参数
			map.putAll(RequestContext.getContext().getResponseSystemParams());
			for(Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();){
				if(it.next().getValue() == null){
					it.remove();
				}
			}
			resp = map;
		}		
		return resp;
	}
	
	/**
	 * @author danyuan
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 */
	private Map<String, Object> baseResponseWrapper(Object resp) throws JsonProcessingException, IOException {
		//ObjectMapper mapper = new ObjectMapper();
		//mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(resp));
		Map<String, Object> map = new HashMap<>();
		if(RequestContext.getContext().getResponseWrapper() != null){//系统签名为
			map.put(responseWrapperProperties.getSign(), RequestContext.getContext().getResponseWrapper().getSign());
			map.put(responseWrapperProperties.getTimestamp(), RequestContext.getContext().getResponseWrapper().getTimestamp());
		}	
		if(json.hasNonNull(responseWrapperProperties.getCode())){
			map.put(responseWrapperProperties.getCode(), json.get(responseWrapperProperties.getCode()).asInt());
		}
		if(json.hasNonNull(responseWrapperProperties.getSubCode())){
			map.put(responseWrapperProperties.getSubCode(), json.get(responseWrapperProperties.getSubCode()).asText());
		}
		if(json.hasNonNull(responseWrapperProperties.getMsg())){
			map.put(responseWrapperProperties.getMsg(), json.get(responseWrapperProperties.getMsg()).asText());
		}	
		if(json.hasNonNull(responseWrapperProperties.getSign())){
			map.put(responseWrapperProperties.getSign(), json.get(responseWrapperProperties.getSign()).asText());
		}
		if(json.hasNonNull(responseWrapperProperties.getTimestamp())){
			map.put(responseWrapperProperties.getTimestamp(), json.get(responseWrapperProperties.getTimestamp()).asText());
		}
		if(json.hasNonNull(responseWrapperProperties.getData())){
			map.put(responseWrapperProperties.getData(), json.get(responseWrapperProperties.getData()));
		}			
		return map;
	}

	private boolean hasResponseWrapper(Object resp) throws JsonProcessingException, IOException{
		if(resp instanceof DefaultResponseWrapper){
			return true;
		}else{
			Set<String> fieldNames = DECLARED_FIELD_NAMES_CACHE.get(resp.getClass());
			Map<String, Boolean> fieldStatusMap = new HashMap<>();
			fieldStatusMap.put(responseWrapperProperties.getCode(), false);
			fieldStatusMap.put(responseWrapperProperties.getSubCode(), false);
			fieldStatusMap.put(responseWrapperProperties.getData(), false);
			fieldStatusMap.put(responseWrapperProperties.getMsg(), false);
			fieldStatusMap.put(responseWrapperProperties.getSign(), false);
			fieldStatusMap.put(responseWrapperProperties.getTimestamp(), false);
			for(String fieldName : fieldNames){
				if(fieldStatusMap.containsKey(fieldName)){
					fieldStatusMap.put(fieldName, true);
				}
			}
			for(boolean status : fieldStatusMap.values()){
				if(!status){
					return false;
				}
			}
			return true;
		}		
	}

	
	private Map<String,Object> successResponseWrapper(){
		Map<String, Object> map = new HashMap<>();
		map.put(responseWrapperProperties.getCode(), ErrorCodes.SUCCESS.getCode());
		if(RequestContext.getContext().getResponseWrapper() != null){
			map.put(responseWrapperProperties.getSign(), RequestContext.getContext().getResponseWrapper().getSign());
			map.put(responseWrapperProperties.getTimestamp(), RequestContext.getContext().getResponseWrapper().getTimestamp());
		}		
		return map;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public int order() {
		return -1;
	}


}
