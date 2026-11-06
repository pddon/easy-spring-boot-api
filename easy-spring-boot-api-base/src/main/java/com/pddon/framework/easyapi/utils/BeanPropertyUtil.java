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
import java.util.concurrent.ConcurrentHashMap;
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

	/**
	 * 类的字段列表缓存，避免每次请求都重复扫描继承链并反射获取字段。
	 * 使用ClassValue按类隔离，类卸载时缓存随之释放，避免静态缓存持有类加载器
	 */
	private static final ClassValue<List<Field>> ALL_FIELD_CACHE = new ClassValue<List<Field>>() {
		@Override
		protected List<Field> computeValue(Class<?> clazz) {
			List<Field> scanned = new ArrayList<>();
			Class<?> currentClass = clazz;
			//当父类为null的时候说明到达了最上层的父类(Object类).
			while (currentClass != null && !"java.lang.Object".equals(currentClass.getName())) {
				scanned.addAll(Arrays.asList(currentClass.getDeclaredFields()));
				currentClass = currentClass.getSuperclass(); //得到父类,然后赋给自己
			}
			return Collections.unmodifiableList(scanned);
		}
	};
	/**
	 * 类注解缓存，避免每次处理对象时都复制注解数组
	 */
	private static final ClassValue<Annotation[]> CLASS_ANNOTATION_CACHE = new ClassValue<Annotation[]>() {
		@Override
		protected Annotation[] computeValue(Class<?> clazz) {
			return clazz.getAnnotations();
		}
	};
	/**
	 * 字段是否存在公共getter的缓存，避免每个字段都触发一次反射查找与异常。
	 * 以Field（声明类+字段名+类型）为key，避免子类与父类同名字段（类型不同）互相串值
	 */
	private static final ClassValue<Map<Field, Boolean>> FIELD_GETTER_CACHE =
			new ClassValue<Map<Field, Boolean>>() {
				@Override
				protected Map<Field, Boolean> computeValue(Class<?> clazz) {
					return new ConcurrentHashMap<>();
				}
			};

	/**
	 * 可序列化字段缓存：按“声明类 + 忽略注解”缓存通过getter校验、且已设置为可访问的字段列表。
	 * 原先每次遍历对象都要对每个字段重复做getter判定查询、注解扫描和setAccessible反射调用；
	 * 字段清单、getter与可访问性在运行期不变，缓存后每个字段只需一次反射取值。
	 * 使用ClassValue按类隔离，类卸载时缓存随之释放，避免静态缓存持有类加载器。
	 */
	private static final ClassValue<Map<Class<?>, List<Field>>> SERIALIZABLE_FIELD_CACHE =
			new ClassValue<Map<Class<?>, List<Field>>>() {
				@Override
				protected Map<Class<?>, List<Field>> computeValue(Class<?> clazz) {
					return new ConcurrentHashMap<>();
				}
			};

	/**
	 * 不忽略任何注解时的缓存key（ConcurrentHashMap不允许null key）
	 */
	private static final Class<?> NO_IGNORE_ANNO = Void.class;

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
		if(!isBaseType(object) && !(object instanceof Map) && !(object instanceof Collections) && !(object instanceof Iterator) && !(object instanceof Collection<?>)){//非基础数据类型需要防止重复引用的问题
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
						sdf.setTimeZone(TimeZone.getDefault());
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
            }else{//普通对象
            	List<Field> fields = getSerializableFields(object.getClass(), ignoreAnno);
                for (Field field : fields) {
                	if(!field.isAccessible()){
                		field.setAccessible(true);
                	}
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
			//空值不做类型判断，直接交给装饰函数；是否返回null由装饰函数决定（内置处理器均原样返回）
			return fun.decorate(fieldName, object, annotations);
		}
		if(!isBaseType(object) && !(object instanceof Map) && !(object instanceof Collections) && !(object instanceof Iterator)){//非基础数据类型需要防止重复引用的问题
			if(hashCodeSet.contains(String.valueOf(object.hashCode()))){
				return fun.decorate(fieldName, object, annotations);
			}
			hashCodeSet.add(String.valueOf(object.hashCode()));
		}		
		Annotation[] currentAnnotations = getClassAnnotations(object.getClass());//获取类上的注解
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
                List<Field> fields = getSerializableFields(object.getClass(), null);
                for (Field field : fields) {
                	if(!field.isAccessible()){
                		field.setAccessible(true);
                	}
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
        } catch (Exception e) {
            log.error(IOUtils.getThrowableInfo(e));
        }
		return object;
	}
	
	public static List<Field> getAllField(Object object){
		//返回副本，保持原有"调用方可自由修改返回集合"的语义
		return new ArrayList<>(getAllFieldCached(object.getClass()));
	}

	private static List<Field> getAllFieldCached(Class<?> clazz){
		return ALL_FIELD_CACHE.get(clazz);
	}

	/**
	 * 获取可用于序列化的字段列表（有公共getter、且不是指定忽略注解标注的字段）。
	 * 结果按“类 + 忽略注解类型”缓存，并为字段设置可访问性；返回的列表不可修改。
	 */
	private static List<Field> getSerializableFields(Class<?> clazz, Class<?> ignoreAnno){
		Map<Class<?>, List<Field>> cache = SERIALIZABLE_FIELD_CACHE.get(clazz);
		Class<?> cacheKey = ignoreAnno == null ? NO_IGNORE_ANNO : ignoreAnno;
		List<Field> cached = cache.get(cacheKey);
		if(cached != null){
			return cached;
		}
		List<Field> fields = new ArrayList<>();
		for(Field field : getAllFieldCached(clazz)){
			if(ignoreAnno != null && hasAnnotation(field, ignoreAnno)){
				continue;
			}
			if(!resolvePublicGetter(field, clazz)){
				continue;
			}
			try{
				field.setAccessible(true);
			}catch(Exception ignored){
				//无法设置可访问性时保留原字段，取值阶段会再次尝试并抛出异常，保持原有失败语义
			}
			fields.add(field);
		}
		List<Field> result = Collections.unmodifiableList(fields);
		List<Field> previous = cache.putIfAbsent(cacheKey, result);
		return previous != null ? previous : result;
	}

	private static boolean hasAnnotation(Field field, Class<?> annoClass){
		Annotation[] fieldAnnos = field.getAnnotations();
		if(fieldAnnos == null){
			return false;
		}
		for(Annotation anno : fieldAnnos){
			if(anno.annotationType().equals(annoClass)){
				return true;
			}
		}
		return false;
	}

	private static Annotation[] getClassAnnotations(Class<?> clazz){
		return CLASS_ANNOTATION_CACHE.get(clazz);
	}
	
	public static Method getMethod(Object obj, String methodName) throws Exception{
		return getMethod(obj.getClass(), methodName);
	}

	private static Method getMethod(Class<?> clazz, String methodName) throws Exception{
		Method method = null;	
		Class<?> currentClass = clazz;
		//当父类为null的时候说明到达了最上层的父类(Object类).
		while (currentClass != null && !"java.lang.Object".equals(currentClass.getName())) {
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
		Class<?> clazz = obj.getClass();
		Map<Field, Boolean> getterCache = FIELD_GETTER_CACHE.get(clazz);

		Boolean exists = getterCache.get(field);
		if(exists == null){
			exists = resolvePublicGetter(field, obj);
			getterCache.put(field, exists);
		}
		return exists;
	}

	private static boolean resolvePublicGetter(Field field, Object obj){
		return resolvePublicGetter(field, obj.getClass());
	}

	/**
	 * 判断字段是否存在公共getter；只依赖字段声明信息与类结构，与具体实例无关。
	 */
	private static boolean resolvePublicGetter(Field field, Class<?> clazz){
		String name = field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);	
		try {
			String getterMethodName = "get"+name;
			
			boolean exists = getMethod(clazz, getterMethodName) != null;
			if(exists){
				return true;
			}
		} catch (Exception e) {			
		}
		if(field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)){
			try {
				String booleanGetterMethodName = "is"+name;
				return getMethod(clazz, booleanGetterMethodName) != null; 
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
		if(object instanceof String){
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
