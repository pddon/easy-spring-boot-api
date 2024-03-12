/**  
* Title DecorateContentFunction.java  
* Description  对字段内容进行额外处理，如加密、解密、国际化等等
* @author danyuan
* @date Nov 15, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.client.annotations;

import java.lang.annotation.Annotation;
import java.util.Map;

@FunctionalInterface
public interface DecorateContentFunction {
	Object decorate(String fieldName, Object data, Map<Class<?>, Annotation> annotations) throws Throwable;
}
