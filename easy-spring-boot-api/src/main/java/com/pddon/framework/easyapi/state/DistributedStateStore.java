/**
* Title DistributedStateStore.java
* Description 跨请求有状态能力的统一存储抽象：本地实现保持原有行为，Redis实现提供多实例一致性
* @author pddon
* @date 2026-11-05
* @version 1.0.0
* site: pddon.cn
*/
package com.pddon.framework.easyapi.state;

/**
 * 统一状态存储抽象。
 * <p>实现分为两类：</p>
 * <p>1、本地实现（LocalStateStore）：单JVM内有效，行为与框架原有的本地缓存一致；</p>
 * <p>2、Redis实现（RedisStateStore）：多实例共享，所有"读取-判定-写入"类操作均保证原子性。</p>
 * <p>值统一使用字符串（必要时由调用方序列化为JSON），避免JDK序列化带来的类版本耦合。</p>
 */
public interface DistributedStateStore {

	/**
	 * 存储类型：local / redis
	 */
	String getType();

	/**
	 * key不存在时写入，存在时不写入。用于"占用"类语义（防重码占用、nonce消费、幂等标记），必须原子。
	 * @param key 业务key（不含全局前缀，由实现内部拼接）
	 * @param value 字符串值
	 * @param ttlSeconds 过期时间（秒），非正数时使用实现默认值
	 * @return true表示本次写入成功（key原本不存在）
	 */
	boolean setIfAbsent(String key, String value, long ttlSeconds);

	/**
	 * 读取值
	 * @param ttlSeconds 本地实现用于定位缓存桶；Redis实现忽略该参数
	 */
	String get(String key, long ttlSeconds);

	/**
	 * 写入/覆盖值
	 */
	void set(String key, String value, long ttlSeconds);

	/**
	 * 删除key
	 * @param ttlSeconds 本地实现用于定位缓存桶；Redis实现忽略该参数
	 */
	boolean delete(String key, long ttlSeconds);

	/**
	 * 是否存在
	 */
	boolean exists(String key, long ttlSeconds);

	/**
	 * 只修改过期时间，不读写值。用于会话续期等场景，避免"读取-覆盖写"丢失并发更新。
	 * @return true表示key存在且过期时间已更新
	 */
	boolean expire(String key, long ttlSeconds);

	/**
	 * 原子自增，返回自增后的值；key不存在时从0开始，并按ttlSeconds设置过期时间
	 */
	long increment(String key, long delta, long ttlSeconds);

	/**
	 * 按key前缀批量删除（谨慎使用，遍历成本较高，仅用于管理类场景）
	 * @param keyPrefix 业务key前缀（不含全局前缀）
	 * @return 删除的条目数
	 */
	long deleteByPrefix(String keyPrefix);
}
