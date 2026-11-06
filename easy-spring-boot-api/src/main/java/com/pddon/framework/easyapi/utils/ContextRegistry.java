/**  
* Title ContextRegistry.java  
* Description  按Spring上下文隔离的注册表
* @author danyuan
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

/**
 * 按Spring上下文隔离的实例注册表。
 *
 * 解决"拦截器/处理器在构造函数中注册到静态集合"导致的跨上下文驻留问题：
 * 同一JVM中同时存在多个上下文（测试、热重载、多租户多上下文）时，
 * 每个上下文只使用自己注册的实例，不会串用其它上下文的实例；
 * 上下文关闭时（Bean的destroy回调）自动清理，避免静态集合持续增长。
 *
 * 上下文以弱引用（{@link WeakReference}）为键，配合{@link ReferenceQueue}：
 * 即使调用方"创建了上下文却忘记close"，只要外部不再持有上下文强引用，
 * 上下文仍可被GC回收，对应条目在下一次register/get/unregister/size调用时被清理。
 * 不新增线程、不加定时任务。
 *
 * 手工通过静态方法注册（不经过Spring）的实例仍保留在调用方的静态集合中，
 * 作为公共注册API的兼容路径。
 */
public final class ContextRegistry<T> {

	private final Map<ContextKey, List<T>> instances = new HashMap<ContextKey, List<T>>();

	private final ReferenceQueue<ApplicationContext> staleContexts = new ReferenceQueue<ApplicationContext>();

	/**
	 * 弱引用键：按上下文对象身份（identity）比较，哈希值在构造时固定。
	 * 引用被清除后仍可用该键对象本身从map中删除条目。
	 */
	private static final class ContextKey extends WeakReference<ApplicationContext> {

		private final int identityHash;

		private ContextKey(ApplicationContext context, ReferenceQueue<ApplicationContext> queue) {
			super(context, queue);
			this.identityHash = System.identityHashCode(context);
		}

		@Override
		public int hashCode() {
			return identityHash;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof ContextKey)) {
				return false;
			}
			ApplicationContext mine = get();
			//两个键只有在各自引用仍存活且指向同一上下文时才算相等
			return mine != null && mine == ((ContextKey) other).get();
		}
	}

	/**
	 * 构造用于查找的键（不注册到ReferenceQueue，不影响GC）
	 */
	private ContextKey lookupKey(ApplicationContext context) {
		return new ContextKey(context, null);
	}

	/**
	 * 清理已被GC回收的上下文条目；只在本类方法内部持有锁时调用
	 */
	private void cleanup() {
		ContextKey stale;
		while ((stale = (ContextKey) staleContexts.poll()) != null) {
			instances.remove(stale);
		}
	}

	/**
	 * 注册实例到指定上下文，并将其从手工注册集合中移除，避免同一实例被重复纳入拦截链
	 */
	public void register(ApplicationContext context, T instance, List<T> legacyList, Comparator<T> comparator) {
		if (context == null || instance == null) {
			return;
		}
		legacyList.remove(instance);
		synchronized (instances) {
			cleanup();
			List<T> list = instances.get(lookupKey(context));
			if (list == null) {
				list = new ArrayList<T>();
				//只有真正建条目的键才注册到ReferenceQueue，查找用的键不注册
				instances.put(new ContextKey(context, staleContexts), list);
			}
			if (!list.contains(instance)) {
				list.add(instance);
				list.sort(comparator);
			}
		}
	}

	/**
	 * 从指定上下文注销实例；上下文内无实例时移除该上下文的引用，避免上下文无法回收
	 */
	public void unregister(ApplicationContext context, T instance) {
		if (context == null) {
			return;
		}
		synchronized (instances) {
			cleanup();
			ContextKey key = lookupKey(context);
			List<T> list = instances.get(key);
			if (list == null) {
				return;
			}
			list.remove(instance);
			if (list.isEmpty()) {
				instances.remove(key);
			}
		}
	}

	/**
	 * 获取指定上下文已注册的实例快照；未注册过返回null，由调用方回退到手工注册集合
	 */
	public List<T> get(ApplicationContext context) {
		if (context == null) {
			return null;
		}
		synchronized (instances) {
			cleanup();
			List<T> list = instances.get(lookupKey(context));
			return list == null ? null : new ArrayList<T>(list);
		}
	}

	/**
	 * 当前已登记的上下文数量，仅用于测试与诊断
	 */
	public int size() {
		synchronized (instances) {
			cleanup();
			return instances.size();
		}
	}
}
