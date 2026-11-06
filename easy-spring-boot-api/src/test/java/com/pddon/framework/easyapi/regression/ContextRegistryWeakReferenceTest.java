package com.pddon.framework.easyapi.regression;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.pddon.framework.easyapi.utils.ContextRegistry;

/**
 * ContextRegistry 弱引用键的行为测试：
 * 1. 注册/查询/注销的返回语义与改造前一致（注销后返回null，重复注销幂等）；
 * 2. 多上下文相互隔离，注销一个上下文不影响另一个；
 * 3. 上下文"不close且外部强引用释放"后，条目在下一次访问时被清理。
 *
 * 上下文close后条目移除由 CrossContextInterceptorIsolationTest 覆盖（依赖Bean的destroy回调）。
 */
public class ContextRegistryWeakReferenceTest {

	private static final Comparator<Object> NO_ORDER = new Comparator<Object>() {
		@Override
		public int compare(Object one, Object other) {
			return 0;
		}
	};

	@Test
	public void testRegisterGetAndUnregisterSemantics() {
		ContextRegistry<Object> registry = new ContextRegistry<Object>();
		List<Object> legacy = new ArrayList<Object>();
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		Object first = new Object();
		Object second = new Object();
		legacy.add(first);
		legacy.add(second);
		try {
			context.refresh();
			registry.register(context, first, legacy, NO_ORDER);
			registry.register(context, second, legacy, NO_ORDER);

			List<Object> snapshot = registry.get(context);
			Assert.assertNotNull(snapshot);
			Assert.assertEquals(2, snapshot.size());
			Assert.assertTrue(snapshot.contains(first));
			Assert.assertTrue(snapshot.contains(second));
			//注册到上下文后，实例从手工注册集合中移除
			Assert.assertTrue(legacy.isEmpty());

			registry.unregister(context, first);
			List<Object> remaining = registry.get(context);
			Assert.assertNotNull(remaining);
			Assert.assertEquals(1, remaining.size());
			Assert.assertTrue(remaining.contains(second));

			registry.unregister(context, second);
			Assert.assertNull(registry.get(context));

			//重复注销必须幂等
			registry.unregister(context, second);
			Assert.assertNull(registry.get(context));
			Assert.assertEquals(0, registry.size());
		} finally {
			context.close();
		}
	}

	@Test
	public void testContextsAreIsolatedAndRemovalDoesNotAffectOthers() {
		ContextRegistry<Object> registry = new ContextRegistry<Object>();
		AnnotationConfigApplicationContext first = new AnnotationConfigApplicationContext();
		AnnotationConfigApplicationContext second = new AnnotationConfigApplicationContext();
		Object firstInstance = new Object();
		Object secondInstance = new Object();
		try {
			first.refresh();
			second.refresh();
			registry.register(first, firstInstance, new ArrayList<Object>(), NO_ORDER);
			registry.register(second, secondInstance, new ArrayList<Object>(), NO_ORDER);

			Assert.assertEquals(1, registry.get(first).size());
			Assert.assertEquals(1, registry.get(second).size());
			Assert.assertFalse(registry.get(first).contains(secondInstance));

			registry.unregister(first, firstInstance);
			Assert.assertNull(registry.get(first));
			Assert.assertEquals(1, registry.get(second).size());
			Assert.assertEquals(1, registry.size());
		} finally {
			first.close();
			second.close();
		}
	}

	@Test
	public void testUnreachableContextEntryIsCleanedUp() throws Exception {
		ContextRegistry<Object> registry = new ContextRegistry<Object>();
		Object instance = new Object();

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.refresh();
		registry.register(context, instance, new ArrayList<Object>(), NO_ORDER);

		WeakReference<AnnotationConfigApplicationContext> contextRef =
				new WeakReference<AnnotationConfigApplicationContext>(context);
		//不关闭上下文，仅释放外部强引用（模拟"创建上下文却忘记close"）
		context = null;
		instance = null;

		//允许等待上限10秒：每轮触发一次GC并检查条目是否已清理
		long deadline = System.currentTimeMillis() + 10000L;
		boolean cleaned = false;
		while (System.currentTimeMillis() < deadline) {
			System.gc();
			Thread.sleep(200L);
			if (registry.size() == 0) {
				cleaned = true;
				break;
			}
		}
		Assert.assertTrue(
				"弱引用条目在10秒内未被清理（contextCollected=" + (contextRef.get() == null)
						+ ", registrySize=" + registry.size() + "）",
				cleaned);
		Assert.assertNull("上下文强引用释放后应可被GC回收", contextRef.get());
	}
}
