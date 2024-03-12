/**  
* Title MethdInvokeUtil.java  
* Description  
* @author danyuan
* @date Dec 19, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.utils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pddon.framework.easyapi.annotation.Decrypt;
import com.pddon.framework.easyapi.annotation.IgnoreSign;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import com.pddon.framework.easyapi.dto.ApiRequestParameter;

public class MethdInvokeUtil {
	public static List<ApiRequestParameter> parseParameters(String[] params, Object [] args, 
			Annotation[][] annotations, Annotation[] methodAnnos){
		List<ApiRequestParameter> parameters = new ArrayList<>();
		//提取业务请求参数
		ApiRequestParameter param = null;
	    for(int i=0; i<args.length; i++){
	    	if(args[i] != null && (BeanPropertyUtil.isBaseType(args[i]) 
	    			|| (args[i] instanceof Serializable))){
	    		param = new ApiRequestParameter();
	    		if(BeanPropertyUtil.isBaseType(args[i])){
	    			String paramName = getBaseTypeParamName(annotations[i]);
	    			if(StringUtils.isEmpty(paramName)){
	    				param.setParamName(params[i]);
	    			}else{
	    				param.setParamName(paramName);
	    			}    			
	    		}else if(params != null){
	    			param.setParamName(params[i]);
	    		}else{
	    			param.setParamName("p"+i);
	    		}	    		
	    		if(checkIsAnno(annotations[i], IgnoreSign.class)){
	    			param.setNeedSign(false);
	    		}else{
	    			param.setNeedSign(true);
	    		}
	    		if(checkIsAnno(annotations[i], Decrypt.class)){
	    			param.setDecrpt(true);
	    		}else{
	    			param.setDecrpt(false);
	    		}
	    		param.setParam(args[i]);
	    		
  		
	    		Annotation[] allAnnos = null;
	    		if(methodAnnos != null && (annotations[i] != null)){
	    			allAnnos = Arrays.copyOf(methodAnnos, methodAnnos.length + annotations[i].length);//数组扩容
	    			System.arraycopy(annotations[i], 0, allAnnos, methodAnnos.length, annotations[i].length);
	    		}else if(methodAnnos != null){
	    			allAnnos = methodAnnos;
	    		}else if(annotations[i] != null){
	    			allAnnos = annotations[i];
	    		}	    		
	    		param.setAnnotations(allAnnos);
	    		param.setIndex(i);
	    		param.setArgs(args);
	    		parameters.add(param);
	    	}
	    }		
		return parameters;
	}
	
	public static boolean checkIsAnno(Annotation[] annotations, Class<?> annoClass){
		for(Annotation anno : annotations){
			if(anno.annotationType().equals(annoClass)){
				return true;
			}
		}
		return false;
	}
	
	public static String getBaseTypeParamName(Annotation[] annotations){
		for(Annotation anno : annotations){
			if(anno.annotationType().equals(RequestParam.class)){
				RequestParam reqAnno = (RequestParam) anno;
				return reqAnno.value();				
			}
		}
		return null;
	}

}
