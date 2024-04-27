/**  
 * Title BeanPropertyUtil.java  
 * Description  
 * @author danyuan
 * @date Jul 31, 2019
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.utils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pddon.framework.easyapi.lambda.DecorateContentFunction;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

@Slf4j
public class BeanPropertyUtil {
	
	private static Pattern linePattern = Pattern.compile("_(\\w)");
	
	private static Pattern humpPattern = Pattern.compile("[A-Z]");

	public static String[] getNullPropertyNames(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

		Set<String> emptyNames = new HashSet<String>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue == null)
				emptyNames.add(pd.getName());
		}
		String[] result = new String[emptyNames.size()];
		return emptyNames.toArray(result);
	}

	public static Map<String, String> objToStringMap(Object object, String parentKey) {
		return objToStringMap(object, parentKey,  null);
	}
	public static Map<String, String> objToStringMap(Object object, String parentKey, Class<?> ignoreAnno) {
		Set<String> hashCodeSet = new HashSet<>();//防止对象间引用关系存在环状结构
		return objToStringMap(object, parentKey, hashCodeSet, ignoreAnno);
	}
	@SuppressWarnings("unchecked")
	public static Map<String, String> objToStringMap(Object object, String parentKey, Set<String> hashCodeSet, Class<?> ignoreAnno) {			
		Map<String, String> resultMap = new HashMap<>();
		if(object == null){
			return resultMap;
		}
		if(!isBaseType(object)){//非基础数据类型需要防止重复引用的问题
			if(hashCodeSet.contains(String.valueOf(object.hashCode()))){
				return resultMap;
			}
			hashCodeSet.add(String.valueOf(object.hashCode()));
		}		
        if(parentKey == null){
        	parentKey = "";
        }
        try {
            if (isBaseType(object)){
                //如果object是null/基本数据类型/包装类/日期类型，则不需要在递归调用
            	if(object != null){
					if(object.getClass().equals(Date.class)){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
						resultMap.put(parentKey, sdf.format((Date)object));
					}else{
						resultMap.put(parentKey, object.toString());
					}
            	}            	
            }else if (object instanceof Map<?,?>){
                Map<?,?> map = (Map<?,?>)object;
                if (map!=null && map.size()>0){
                    for (Object key : map.keySet()){
                    	
                        Object val = map.get(key);
                        String currentKey = key.toString();
                        if(!StringUtils.isEmpty(parentKey)){
                    		currentKey = parentKey + "." + key;
                    	}
						if(val != null){
							if(isBaseType(val)){
								resultMap.put(currentKey, val.toString());
							}else if(val instanceof Serializable){
								resultMap.putAll(objToStringMap(val, currentKey, hashCodeSet, ignoreAnno));
							}
						}
                    }
                }
            }else if(object.getClass().isArray() && !isIgnoreType(object.getClass().getComponentType())){//数组
            	Object[] arr = (Object[]) object;            	
            	for(int i=0; i<arr.length; i++){
            		Object subObj = arr[i];
                	String currentKey = "";
                	if(!StringUtils.isEmpty(parentKey)){
                		currentKey = parentKey+"["+String.valueOf(i)+"]";
                	}
					if(subObj != null){
						resultMap.putAll(objToStringMap(subObj, currentKey, hashCodeSet, ignoreAnno));
					}
            	}
            }else if(object instanceof List<?>){//List集合
            	List<Object> list = (List<Object>)object;
            	for(int i=0; i < list.size(); i++){
            		Object subObj = list.get(i);
            		String currentKey = "";
                	if(!StringUtils.isEmpty(parentKey)){
                		currentKey = parentKey+"["+String.valueOf(i)+"]";
                	}
					if(subObj != null){
						resultMap.putAll(objToStringMap(subObj, currentKey, hashCodeSet, ignoreAnno));
					}
            	}            	      	
            }else if(object instanceof Set<?>){//Set集合
            	Set<Object> set = (Set<Object>)object;
            	Set<Object> newSet = new HashSet<Object>();
            	int i = 0;
            	for(Object subObj : set){
            		String currentKey = "";
                	if(!StringUtils.isEmpty(parentKey)){
                		currentKey = parentKey+"["+String.valueOf(i++)+"]";
                	}
					if(subObj != null){
						resultMap.putAll(objToStringMap(subObj, currentKey, hashCodeSet, ignoreAnno));
					}
            	} 
            	set.removeAll(set);
            	set.addAll(newSet);
            }else{//普通对象
            	List<Field> fields = getAllField(object);
            	boolean ignoreField = false;
                for (Field field : fields) {
                	ignoreField = false;
                	if(ignoreAnno != null){
                		//判断该字段是否需要忽略
                    	Annotation[] fieldAnnos= field.getAnnotations();
                    	if(fieldAnnos != null){
                			for(Annotation anno : fieldAnnos){
                				if(anno.annotationType().equals(ignoreAnno)){
                					ignoreField = true;
                					break;
                				}
                			}
                		}   
                	}                	
                	if(!ignoreField && hasPublicGetter(field, object)){
                		field.setAccessible(true);
                    	Object subObj = field.get(object);
                    	String subKeyName = field.getName();
                    	String currentKey = subKeyName;
                    	if(!StringUtils.isEmpty(parentKey)){
                    		currentKey = parentKey+"."+currentKey;
                    	}
						if(subObj != null){
							resultMap.putAll(objToStringMap(subObj, currentKey, hashCodeSet, ignoreAnno));
						}
                	}                	
                }
            } 
        } catch (Exception e) {
            log.error(IOUtils.getThrowableInfo(e));
        }
		return resultMap;
	}
	
	public static Object decorateObj(String fieldName, Object object, Map<Class<?>, Annotation> annotations, DecorateContentFunction fun) throws Throwable{
		Set<String> hashCodeSet = new HashSet<>();//防止对象间引用关系存在环状结构
		return decorateObj(fieldName, object, annotations, fun, hashCodeSet);
	}
	
	/**
	 * 为对象数据进行处理
	 * @author danyuan
	 * @throws Throwable 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object decorateObj(String fieldName, Object object, Map<Class<?>, Annotation> annotations, DecorateContentFunction fun, Set<String> hashCodeSet) throws Throwable {
		if(object == null){
			return fun.decorate(fieldName, object, annotations);
		}
		if(!isBaseType(object)){//非基础数据类型需要防止重复引用的问题
			if(hashCodeSet.contains(String.valueOf(object.hashCode()))){
				return fun.decorate(fieldName, object, annotations);
			}
			hashCodeSet.add(String.valueOf(object.hashCode()));
		}		
		Annotation[] currentAnnotations = object.getClass().getAnnotations();//获取类上的注解
		Map<Class<?>, Annotation> allAnnos = new HashMap<>();
		if(annotations != null){
			allAnnos.putAll(annotations);
		}
		if(currentAnnotations != null){
			for(Annotation anno : currentAnnotations){
				allAnnos.put(anno.annotationType(), anno);
			}
		}		
        try {
            if (object==null || isBaseType(object)){
                //如果object是null/基本数据类型/包装类/日期类型，则不需要在递归调用
            	if(object != null){            		
					return fun.decorate(fieldName, object, allAnnos);
            	}            	
            }else if (object instanceof Map<?,?>){
                Map<Object,Object> map = (Map)object;
                if (map!=null && map.size()>0){
                    for (Object key : map.keySet()){                    	
                        Object val = map.get(key);    
                        try{
                        	if(isBaseType(val)){
                            	map.put(key, (Object)fun.decorate(fieldName + key, val, allAnnos));
                            }else{
                            	map.put(key, decorateObj(fieldName + key, val, allAnnos, fun, hashCodeSet));
                            }
                    	}catch(Exception e){
                    		log.warn(e.getMessage());
                    	}                        
                    }
                }
            }else if(object.getClass().isArray() && !isIgnoreType(object.getClass().getComponentType())){//数组
            	Object[] arr = (Object[]) object;            	
            	for(int i=0; i<arr.length; i++){
            		Object subObj = arr[i];
					if(subObj != null){
						try{
							arr[i]=decorateObj(fieldName + i, subObj, allAnnos, fun, hashCodeSet);
						}catch(Exception e){
							log.warn(e.getMessage());
						}
					}
            	}
            }else if(object instanceof List<?>){//List集合
            	List<Object> list = (List<Object>)object;
            	for(int i=0; i < list.size(); i++){
            		Object subObj = list.get(i);
					if(subObj != null){
						try{
							list.set(i, decorateObj(fieldName + i, subObj, allAnnos, fun, hashCodeSet));
						}catch(Exception e){
							log.warn(e.getMessage());
						}
					}
            	}            	      	
            }else if(object instanceof Set<?>){//Set集合
            	Set<Object> set = (Set<Object>)object;
            	Set<Object> newSet = new HashSet<Object>();
            	int i = 0;
            	for(Object subObj : set){
					if(subObj != null){
						try{
							newSet.add(decorateObj(fieldName + i++, subObj, allAnnos, fun, hashCodeSet));
						}catch(Exception e){
							log.warn(e.getMessage());
						}
					}
            	} 
            	set.removeAll(set);
            	set.addAll(newSet);
            }else{//普通对象
                List<Field> fields = getAllField(object);
                for (Field field : fields) {
                	if(hasPublicGetter(field, object)){
                		field.setAccessible(true);
                    	Object subObj = field.get(object);
                    	Annotation[] fieldAnnos= field.getAnnotations();
						HashMap<Class<?>, Annotation> allFieldAnnos = new HashMap<>(allAnnos);
                    	if(fieldAnnos != null){
                			for(Annotation anno : fieldAnnos){
								allFieldAnnos.put(anno.annotationType(), anno);
                			}
                		}
						if(subObj != null){
							try{
								field.set(object, decorateObj(field.getName(), subObj, allFieldAnnos, fun, hashCodeSet));
							}catch(Exception e){
								log.warn(e.getMessage());
							}
						}
                	}                	
                }
            } 
        } catch (Exception e) {
            log.error(IOUtils.getThrowableInfo(e));
        }
		return object;
	}
	
	public static List<Field> getAllField(Object object){
		List<Field> fieldList = new ArrayList<>() ;
		Class<?> currentClass = object.getClass();
		//当父类为null的时候说明到达了最上层的父类(Object类).
		while (currentClass !=null && !currentClass.getName().toLowerCase().equals("java.lang.object")) {
		      fieldList.addAll(Arrays.asList(currentClass .getDeclaredFields()));
		      currentClass = currentClass.getSuperclass(); //得到父类,然后赋给自己
		}
		return fieldList;
	}
	
	public static Method getMethod(Object obj, String methodName) throws Exception{
		Method method = null;	
		Class<?> currentClass = obj.getClass();
		//当父类为null的时候说明到达了最上层的父类(Object类).
		while (currentClass !=null && !currentClass.getName().toLowerCase().equals("java.lang.object")) {
			try{
				method = currentClass.getDeclaredMethod(methodName);	
			}catch(Exception e){				
			}
			
			if(method != null){
				break;
			}
		    currentClass = currentClass.getSuperclass(); //得到父类,然后赋给自己
		}
		return method;
	}
	
	public static boolean hasPublicGetter(Field field, Object obj){
		String name = field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);	
		try {
			String getterMethodName = "get"+name;
			
			boolean exists = getMethod(obj, getterMethodName) != null;
			if(exists){
				return true;
			}
		} catch (Exception e) {			
		}
		if(field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)){
			try {
				String booleanGetterMethodName = "is"+name;
				return getMethod(obj, booleanGetterMethodName) != null; 
			}catch (Exception e) {
			}
		}		
		return false;
	}

	/**
	 * 判断对象属性是否是基本数据类型
	 * @param object
	 * @return
	 */
	public static boolean isBaseType(Object object) {	
		if(object == null){
			return true;
		}
		if(object.getClass().isEnum()){
			return true;
		}
		if(object instanceof Number){
			return true;
		}
		Class className = object.getClass();
	    return isBaseTypeClass(className);
	}
	
	public static boolean isIgnoreType(Class<?> clazz) {	
		if(clazz == null){
			return true;
		}
	    return clazz.equals(Byte.class) ||
	    		clazz.equals(byte.class) ||	    		
	    		clazz.equals(Character.class) ||
	    		clazz.equals(char.class) ||
	    		clazz.equals(Boolean.class) ||
	    		clazz.equals(boolean.class)
	            ;
	}


	/**
	 * 下划线转驼峰 
	 * */
	public static String lineToHump(String str) {
		str = str.toLowerCase();
		Matcher matcher = linePattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	/** 
	 * 驼峰转下划线
	 * */
	public static String humpToLine(String str) {
		Matcher matcher = humpPattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * @author danyuan
	 */
	public static boolean isBaseTypeClass(Class<?> className) {
		return className.equals(String.class)||
	    		className.equals(Integer.class) ||
	            className.equals(int.class) ||
	            className.equals(Byte.class) ||
	            className.equals(byte.class) ||
	            className.equals(Long.class) ||
	            className.equals(long.class) ||
	            className.equals(Double.class) ||
	            className.equals(double.class) ||
	            className.equals(Float.class) ||
	            className.equals(float.class) ||
	            className.equals(Character.class) ||
	            className.equals(char.class) ||
	            className.equals(Short.class) ||
	            className.equals(short.class) ||
	            className.equals(Boolean.class) ||
	            className.equals(boolean.class) ||
	            className.equals(Date.class) ||
	            Number.class.isAssignableFrom(className) ||
	            Void.class.isAssignableFrom(className) ||
	            className.getName().equals("void")
	            ;
	}
}
