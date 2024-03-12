package com.pddon.framework.easyapi.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 分布式锁
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LockDistributed {
    @AliasFor("prefix")
    String value() default "";

    /**
     * 缓存前缀标识,如果不指定则自动生成
     * 自动生成的key前缀的格式： 类名:方法名
     * @author danyuan
     */
    @AliasFor("value")
    String prefix() default "";

    /**
     * 参数表达式，支持同一方法下实现多个指定唯一标识的分布式锁
     * 用于作为Key的标识值
     * 默认该方法不通过参数区分，统一缓存
     * @author danyuan
     */
    String id() default "";

    /**
     * 获取锁时自旋等待的超时时间
     * @return
     */
    long acquireWaitingSeconds() default 2;

    /**
     * 锁自动失效超时时间，默认6分钟超时
     * @return
     */
    long timeoutSeconds() default 360;

    /**
     * 是否自动续约，当检测到锁快超时后，业务还未执行完成时自动续约
     * @return
     */
    boolean autoRefresh() default true;

}
