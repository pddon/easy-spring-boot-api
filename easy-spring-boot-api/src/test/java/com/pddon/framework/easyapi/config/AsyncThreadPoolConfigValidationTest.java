package com.pddon.framework.easyapi.config;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import com.pddon.framework.easyapi.properties.AsyncThreadPoolConfig;

/**
 * 异步线程池启动期配置校验测试：
 * fail-fast=true（默认）保持原有快速失败语义；fail-fast=false时回退默认值并继续启动。
 * 非法拒绝策略不在校验范围，沿用原有"告警+discard"行为。
 */
public class AsyncThreadPoolConfigValidationTest {

	@Test
	public void testValidConfigApplied() {
		AsyncThreadPoolConfig config = new AsyncThreadPoolConfig();
		config.setCorePoolSize(3);
		config.setMaxPoolSize(8);
		config.setQueueCapacity(100);
		config.setKeepAliveSeconds(60);
		config.setThreadNamePrefix("Test-");

		ThreadPoolTaskExecutor executor = executor(config);
		Assert.assertEquals(3, executor.getCorePoolSize());
		Assert.assertEquals(8, executor.getMaxPoolSize());
		Assert.assertEquals(100, queueCapacity(executor));
		Assert.assertEquals(60, executor.getKeepAliveSeconds());
		Assert.assertEquals("Test-", executor.getThreadNamePrefix());
		executor.shutdown();
	}

	@Test
	public void testCoreGreaterThanMaxFailsFastByDefault() {
		AsyncThreadPoolConfig config = new AsyncThreadPoolConfig();
		config.setCorePoolSize(30);
		config.setMaxPoolSize(10);
		try {
			executor(config);
			Assert.fail("corePoolSize > maxPoolSize 时应快速失败");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("maxPoolSize must be >= corePoolSize"));
		}
	}

	@Test
	public void testNegativeCorePoolSizeFailsFast() {
		AsyncThreadPoolConfig config = new AsyncThreadPoolConfig();
		config.setCorePoolSize(-1);
		try {
			executor(config);
			Assert.fail("负数核心线程数应快速失败");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("corePoolSize must be >= 0"));
		}
	}

	@Test
	public void testNegativeQueueCapacityFailsFast() {
		AsyncThreadPoolConfig config = new AsyncThreadPoolConfig();
		config.setQueueCapacity(-1);
		try {
			executor(config);
			Assert.fail("负数队列长度应快速失败");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("queueCapacity must be >= 0"));
		}
	}

	@Test
	public void testZeroMaxPoolSizeFailsFast() {
		AsyncThreadPoolConfig config = new AsyncThreadPoolConfig();
		config.setMaxPoolSize(0);
		try {
			executor(config);
			Assert.fail("maxPoolSize=0 应快速失败");
		} catch (IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage().contains("maxPoolSize must be >= 1"));
		}
	}

	@Test
	public void testFailFastFalseFallsBackToDefaultsForCoreGreaterThanMax() {
		AsyncThreadPoolConfig config = new AsyncThreadPoolConfig();
		config.setFailFast(false);
		config.setCorePoolSize(30);
		config.setMaxPoolSize(10);
		config.setThreadNamePrefix("Keep-");

		ThreadPoolTaskExecutor executor = executor(config);
		Assert.assertEquals(5, executor.getCorePoolSize());
		Assert.assertEquals(20, executor.getMaxPoolSize());
		Assert.assertEquals(10000, queueCapacity(executor));
		Assert.assertEquals(600, executor.getKeepAliveSeconds());
		//合法项保留配置值
		Assert.assertEquals("Keep-", executor.getThreadNamePrefix());
		executor.shutdown();
	}

	@Test
	public void testFailFastFalseFallsBackToDefaultsForNegativeQueueCapacity() {
		AsyncThreadPoolConfig config = new AsyncThreadPoolConfig();
		config.setFailFast(false);
		config.setQueueCapacity(-5);

		ThreadPoolTaskExecutor executor = executor(config);
		Assert.assertEquals(5, executor.getCorePoolSize());
		Assert.assertEquals(20, executor.getMaxPoolSize());
		Assert.assertEquals(10000, queueCapacity(executor));
		executor.shutdown();
	}

	@Test
	public void testCallerRunsPolicyApplied() {
		AsyncThreadPoolConfig config = new AsyncThreadPoolConfig();
		config.setRejectPolicy("callerRuns");
		ThreadPoolTaskExecutor executor = executor(config);
		RejectedExecutionHandler handler = executor.getThreadPoolExecutor().getRejectedExecutionHandler();
		Assert.assertTrue(handler instanceof ThreadPoolExecutor.CallerRunsPolicy);
		executor.shutdown();
	}

	@Test
	public void testUnknownRejectPolicyFallsBackToDiscardWithoutFailure() {
		AsyncThreadPoolConfig config = new AsyncThreadPoolConfig();
		config.setRejectPolicy("unknownPolicy");
		ThreadPoolTaskExecutor executor = executor(config);
		RejectedExecutionHandler handler = executor.getThreadPoolExecutor().getRejectedExecutionHandler();
		Assert.assertNotNull(handler);
		Assert.assertFalse(handler instanceof ThreadPoolExecutor.CallerRunsPolicy);
		executor.shutdown();
	}

	private ThreadPoolTaskExecutor executor(AsyncThreadPoolConfig config) {
		EasyApiAsyncConfigurer configurer = new EasyApiAsyncConfigurer();
		ReflectionTestUtils.setField(configurer, "asyncThreadPoolConfig", config);
		Executor executor = configurer.getAsyncExecutor();
		Assert.assertTrue(executor instanceof ThreadPoolTaskExecutor);
		return (ThreadPoolTaskExecutor) executor;
	}

	/**
	 * 当前Spring版本的ThreadPoolTaskExecutor没有getQueueCapacity()，通过字段读取
	 */
	private int queueCapacity(ThreadPoolTaskExecutor executor) {
		return (Integer) ReflectionTestUtils.getField(executor, "queueCapacity");
	}
}
