/**  
* Title CacheResult.java  
* Description  缓存方法返回值
* 缓存key的格式：
*   前缀:参数提取的值信息
* @author danyuan
* @date Dec 19, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.pddon.framework.easyapi.consts.CacheKeyMode;
import org.springframework.core.annotation.AliasFor;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.consts.CacheExpireMode;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheMethodResult {
	
	@AliasFor("id")
	String value() default "";
	
	/**
	 * 参数表达式
	 * 用于作为Key的标识值
	 * 默认该方法不通过参数区分，统一缓存
	 * @author danyuan
	 */
	@AliasFor("value")
	String id() default "";

	/**
	 * 通过参数值觉得是否启用缓存功能
	 * @return {@link String}
	 * @author: Allen
	 * @Date: 2024/4/19 17:25
	 */
	String needCacheField() default "";
	
	/**
	 * 缓存前缀标识,如果不指定则自动生成
	 * 自动生成的key前缀的格式： 类名:方法名
	 * @author danyuan
	 */
	String prefix() default "";
	/**
	 * 缓存失效时长
	 * @author danyuan
	 */
	long expireSeconds() default 120L;
	/**
	 * 缓存失效模式，默认为最后访问开始计算失效时间
	 * @author danyuan
	 */
	CacheExpireMode expireMode() default CacheExpireMode.EXPIRE_AFTER_REDA;
	/**
	 * 自定义缓存管理器
	 * @author danyuan
	 */
	Class<? extends CacheManager> cacheManager() default CacheManager.class;

	/**
	 * 缓存Key生成策略类型，默认通过所有入参生成
	 * @return
	 */
	CacheKeyMode keyMode() default CacheKeyMode.AUTO_BY_PARAMS;
}
