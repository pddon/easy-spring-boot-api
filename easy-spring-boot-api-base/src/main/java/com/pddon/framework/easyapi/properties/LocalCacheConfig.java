/**
* Title LocalCacheConfig.java
* Description 本地缓存容器配置
* @author pddon
* @date 2026-11-04
* @version 1.0.0
* site: pddon.cn
*/
package com.pddon.framework.easyapi.properties;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地缓存容器配置。
 * 该类位于base模块（easy-spring-boot-api依赖base，方向不可逆），
 * 因此以独立配置项easyapi.local-cache来集中管理，避免循环依赖。
 */
@Component
@ConfigurationProperties(prefix = "easyapi.local-cache")
@Data
public class LocalCacheConfig {
    /**
     * 是否关闭本地缓存容器的全局锁。
     * 默认false，保持原有行为（set/remove/clear串行执行）；
     * 开启后读写路径不再持有全局锁，仅在创建新的缓存管理器时做一次双检锁。
     */
    private boolean lockFree = false;
    /**
     * 单个过期时间桶最多缓存的key数量。
     * 默认100000，与改造前硬编码值一致；调小可降低极端场景下的缓存内存上限，
     * 代价是被淘汰的key需要重新回源。
     */
    private Long maxCacheSize = 100000L;
    /**
     * 最多允许创建的过期时间桶数量。
     * 默认2000，与改造前硬编码值一致；桶数量超过上限后新TTL会回退到默认桶（180秒），
     * 调小可避免业务使用大量不同TTL时长期驻留过多Cache实例。
     */
    private Long maxCacheNum = 2000L;
}
