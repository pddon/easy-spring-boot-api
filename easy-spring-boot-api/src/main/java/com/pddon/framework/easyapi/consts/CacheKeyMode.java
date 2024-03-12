package com.pddon.framework.easyapi.consts;

/**
 * 缓存key生成策略
 */
public enum CacheKeyMode {
    /**
     * 通过指定自定义ID生成
     */
    CUSTOM_ID,
    /**
     * 通过所有参数信息生成
     */
    AUTO_BY_PARAMS,
    /**
     * 通过方法名生成
     */
    AUTO_BY_METHOD
}
