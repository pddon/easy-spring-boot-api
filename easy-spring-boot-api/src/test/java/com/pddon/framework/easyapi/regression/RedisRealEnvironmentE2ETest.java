package com.pddon.framework.easyapi.regression;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.pddon.framework.easyapi.cache.CacheManagerDistributedTest;
import com.pddon.framework.easyapi.cache.LocalCacheContainer;
import com.pddon.framework.easyapi.cache.RedisStateCacheManager;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.dto.Session;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.impl.DefaultCheckRepeatManagerImpl;
import com.pddon.framework.easyapi.impl.DefaultSessionManagerImpl;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import com.pddon.framework.easyapi.security.SignReplayProtector;
import com.pddon.framework.easyapi.state.FallbackStateStore;
import com.pddon.framework.easyapi.state.LocalStateStore;
import com.pddon.framework.easyapi.state.RedisStateStore;

/**
 * 真实Redis环境的端到端验证（默认跳过）。
 *
 * <p>运行方式（密码只通过命令行属性或环境变量传入，不写入任何文件）：</p>
 * <pre>
 * mvn -o -pl easy-spring-boot-api test -DskipTests=false -Dtest=RedisRealEnvironmentE2ETest \
 *   -Dredis.test.enabled=true -Dredis.host=127.0.0.1 -Dredis.port=6379 \
 *   -Dredis.password=*** -Dredis.test.key-prefix=easyapi-e2e-&lt;唯一标记&gt;:
 * </pre>
 *
 * <p>隔离约定：所有测试key都带唯一前缀，只通过SCAN+DEL清理本前缀，不执行FLUSHDB/FLUSHALL，
 * 不使用KEYS；不指定key-prefix时自动生成随机前缀，避免污染环境已有数据。</p>
 */
public class RedisRealEnvironmentE2ETest {

	private static final long BAD_CONNECT_TOKEN_TTL = 60L;

	private String runPrefix;
	private String sentinelPrefix;
	private String sentinelKey;

	private LettuceConnectionFactory factoryA;
	private LettuceConnectionFactory factoryB;
	private LettuceConnectionFactory factoryBad;
	private StringRedisTemplate templateA;
	private StringRedisTemplate templateB;
	private StringRedisTemplate templateBad;

	private RedisStateStore storeA;
	private RedisStateStore storeB;
	private RedisStateCacheManager cacheA;
	private RedisStateCacheManager cacheB;

	@Before
	public void setUp() {
		String enabled = System.getProperty("redis.test.enabled", System.getenv("REDIS_TEST_ENABLED"));
		Assume.assumeTrue("未开启真实Redis环境测试（需 -Dredis.test.enabled=true 或 REDIS_TEST_ENABLED=true），已跳过",
				Boolean.parseBoolean(enabled));

		String host = System.getProperty("redis.host", System.getenv("REDIS_HOST"));
		Assume.assumeTrue("未提供Redis地址（-Dredis.host 或 REDIS_HOST），已跳过", host != null && !host.trim().isEmpty());
		host = host.trim();
		String portValue = System.getProperty("redis.port", System.getenv("REDIS_PORT"));
		int port = portValue == null || portValue.trim().isEmpty() ? 6379 : Integer.parseInt(portValue.trim());
		String password = System.getProperty("redis.password", System.getenv("REDIS_PASSWORD"));

		runPrefix = System.getProperty("redis.test.key-prefix", System.getenv("REDIS_TEST_KEY_PREFIX"));
		if (runPrefix == null || runPrefix.trim().isEmpty()) {
			runPrefix = "easyapi-e2e-" + UUID.randomUUID().toString() + ":";
		} else {
			runPrefix = runPrefix.trim();
			if (!runPrefix.endsWith(":")) {
				runPrefix = runPrefix + ":";
			}
		}
		sentinelPrefix = "easyapi-e2e-sentinel-" + UUID.randomUUID().toString() + ":";
		sentinelKey = sentinelPrefix + "keep";

		factoryA = new LettuceConnectionFactory(host, port);
		factoryB = new LettuceConnectionFactory(host, port);
		factoryBad = new LettuceConnectionFactory(host, port);
		if (password != null && !password.isEmpty()) {
			factoryA.setPassword(password);
			factoryB.setPassword(password);
			factoryBad.setPassword(password + "!wrong");
		} else {
			//服务端未配置密码时，用错误端口模拟故障，避免"空密码"被当作正确密码
			factoryBad.destroy();
			factoryBad = new LettuceConnectionFactory(host, 6399);
		}
		factoryA.afterPropertiesSet();
		factoryB.afterPropertiesSet();
		initializeQuietly(factoryBad);

		templateA = new StringRedisTemplate(factoryA);
		templateA.afterPropertiesSet();
		templateB = new StringRedisTemplate(factoryB);
		templateB.afterPropertiesSet();
		templateBad = new StringRedisTemplate(factoryBad);
		templateBad.afterPropertiesSet();

		storeA = new RedisStateStore(templateA, runPrefix);
		storeB = new RedisStateStore(templateB, runPrefix);
		cacheA = new RedisStateCacheManager(storeA);
		cacheB = new RedisStateCacheManager(storeB);

		//前置检查：前缀必须不存在，避免覆盖环境已有数据
		Assert.assertEquals("测试前缀已存在，请更换 -Dredis.test.key-prefix 或使用随机前缀", 0L, countKeys(templateA, runPrefix + "*"));
		Assert.assertEquals("哨兵前缀已存在", 0L, countKeys(templateA, sentinelPrefix + "*"));
	}

	private void initializeQuietly(LettuceConnectionFactory factory) {
		try {
			factory.afterPropertiesSet();
		} catch (Exception e) {
			//错误密码/错误端口连接工厂允许延迟到首次命令时再暴露异常
		}
	}

	@After
	public void tearDown() {
		if (templateA == null) {
			return;
		}
		long residual = -1L;
		try {
			storeA.deleteByPrefix("");
			//哨兵key单独删除
			templateA.delete(sentinelKey);
			residual = countKeys(templateA, runPrefix + "*");
			long sentinelResidual = countKeys(templateA, sentinelPrefix + "*");
			System.out.println("[redis-e2e] cleanup prefix=" + runPrefix + " residual=" + residual
					+ " sentinelResidual=" + sentinelResidual);
			Assert.assertEquals("清理后测试前缀下仍有残留key", 0L, residual);
			Assert.assertEquals("清理后哨兵前缀下仍有残留key", 0L, sentinelResidual);
		} finally {
			destroyQuietly(factoryA);
			destroyQuietly(factoryB);
			destroyQuietly(factoryBad);
		}
	}

	private void destroyQuietly(LettuceConnectionFactory factory) {
		try {
			factory.destroy();
		} catch (Exception ignored) {
		}
	}

	// ------------------------------------------------------------------
	// 1. 连通性 / 环境信息
	// ------------------------------------------------------------------

	@Test
	public void testConnectionAndServerInfo() {
		String pong = templateA.execute((RedisCallback<String>) RedisConnectionCommands::ping);
		Assert.assertEquals("PONG", pong);

		Properties info = templateA.execute((RedisCallback<Properties>) connection -> connection.info("server"));
		Assert.assertNotNull(info);
		String version = info.getProperty("redis_version");
		Assert.assertNotNull("INFO server 应包含 redis_version", version);

		String maxmemory = configGet(templateA, "maxmemory");
		String maxmemoryPolicy = configGet(templateA, "maxmemory-policy");
		System.out.println("[redis-e2e] PING=" + pong + " redis_version=" + version
				+ " maxmemory=" + maxmemory + " maxmemory-policy=" + maxmemoryPolicy);
	}

	// ------------------------------------------------------------------
	// 2. 防重复提交码并发（两个模拟实例）
	// ------------------------------------------------------------------

	@Test
	public void testNoRepeatSubmitConcurrentOnlyOneWinner() throws Exception {
		EasyApiConfig config = new EasyApiConfig();
		config.setNoSubmitRepeatTimeoutSeconds(120L);

		DefaultCheckRepeatManagerImpl managerA = new DefaultCheckRepeatManagerImpl();
		managerA.setEasyApiConfig(config);
		managerA.setStateStore(storeA);
		DefaultCheckRepeatManagerImpl managerB = new DefaultCheckRepeatManagerImpl();
		managerB.setEasyApiConfig(config);
		managerB.setStateStore(storeB);

		String token = "token-" + UUID.randomUUID().toString();
		List<Runnable> tasks = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			DefaultCheckRepeatManagerImpl manager = (i % 2 == 0) ? managerA : managerB;
			tasks.add(() -> manager.check(token));
		}
		ConcurrencyResult result = runConcurrently(tasks);
		Assert.assertEquals("同一提交码16并发应只有1次成功", 1, result.success);
		Assert.assertEquals("其余15次应抛出防重复提交业务异常", 15, result.businessFailures);
		Assert.assertTrue("不应出现非业务异常:" + result.unexpected, result.unexpected == null);

		String rawKey = runPrefix + "repeat-submit:" + token;
		Assert.assertNotNull("重复提交码应写入Redis", templateA.opsForValue().get(rawKey));
		Long ttl = templateA.getExpire(rawKey);
		Assert.assertTrue("重复提交码TTL应大于0，实际=" + ttl, ttl != null && ttl > 0);
		System.out.println("[redis-e2e] repeat-submit key=" + rawKey + " ttl=" + ttl
				+ " success=" + result.success + " failures=" + result.businessFailures);
	}

	// ------------------------------------------------------------------
	// 3. nonce并发（验签防重放）
	// ------------------------------------------------------------------

	@Test
	public void testNonceConcurrentOnlyOneWinner() throws Exception {
		EasyApiConfig config = new EasyApiConfig();
		SignReplayProtector protectorA = new SignReplayProtector(config, storeA);
		SignReplayProtector protectorB = new SignReplayProtector(config, storeB);

		String nonce = "nonce-" + UUID.randomUUID().toString();
		List<Runnable> tasks = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			SignReplayProtector protector = (i % 2 == 0) ? protectorA : protectorB;
			tasks.add(() -> protector.consumeNonce(nonce));
		}
		ConcurrencyResult result = runConcurrently(tasks);
		Assert.assertEquals("同一nonce 16并发应只有1次成功", 1, result.success);
		Assert.assertEquals("其余15次应判定为重放", 15, result.businessFailures);
		Assert.assertTrue("不应出现非业务异常:" + result.unexpected, result.unexpected == null);

		String rawKey = runPrefix + "sign:nonce:" + nonce;
		Long ttl = templateA.getExpire(rawKey);
		Assert.assertTrue("nonce TTL应大于0，实际=" + ttl, ttl != null && ttl > 0);
		System.out.println("[redis-e2e] nonce key=" + rawKey + " ttl=" + ttl);
	}

	// ------------------------------------------------------------------
	// 4. 方法缓存跨实例（经CacheManager读写）
	// ------------------------------------------------------------------

	@Test
	public void testMethodCacheCrossInstanceReadAndInvalidate() {
		String key = "DemoService:query:a";
		CacheManagerDistributedTest.CacheValue value = new CacheManagerDistributedTest.CacheValue();
		value.setName("method-cache");
		value.setCount(7);

		cacheA.set(key, value, 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);
		CacheManagerDistributedTest.CacheValue loaded = cacheB.get(key,
				CacheManagerDistributedTest.CacheValue.class, 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);
		Assert.assertNotNull("实例B应命中实例A写入的方法缓存", loaded);
		Assert.assertEquals("method-cache", loaded.getName());
		Assert.assertEquals(7, loaded.getCount());

		String rawKey = runPrefix + "cache:write:" + key;
		String raw = templateB.opsForValue().get(rawKey);
		Assert.assertNotNull(raw);
		Assert.assertTrue("值应为JSON信封，实际=" + raw, raw.startsWith("{\"c\":") && raw.contains("\"v\":"));
		Assert.assertTrue("方法缓存TTL应大于0", templateB.getExpire(rawKey) > 0);
		System.out.println("[redis-e2e] method-cache key=" + rawKey + " raw=" + abbreviate(raw)
				+ " ttl=" + templateB.getExpire(rawKey));

		cacheA.remove(key, 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);
		Assert.assertNull("实例A失效后实例B不应再命中", cacheB.get(key,
				CacheManagerDistributedTest.CacheValue.class, 60L, CacheExpireMode.EXPIRE_AFTER_WRITE));
	}

	// ------------------------------------------------------------------
	// 5. 会话跨实例 + refreshAliveTime原子续期（不重写值）
	// ------------------------------------------------------------------

	@Test
	public void testSessionCrossInstanceAndRefreshKeepsValue() {
		DefaultSessionManagerImpl sessionManagerA = new DefaultSessionManagerImpl();
		sessionManagerA.setCacheManager(cacheA);
		sessionManagerA.setExpireSeconds(15L);
		DefaultSessionManagerImpl sessionManagerB = new DefaultSessionManagerImpl();
		sessionManagerB.setCacheManager(cacheB);
		sessionManagerB.setExpireSeconds(15L);

		Session session = new Session();
		session.setAppId("e2e");
		session.setUserId("u1");
		session.setLocale("zh_CN");
		//persist内部会生成sessionId并写入状态存储
		sessionManagerA.persist(session);

		Session loaded = sessionManagerB.get(session.getSessionId());
		Assert.assertNotNull("实例B应读到实例A写入的会话", loaded);
		Assert.assertEquals("u1", loaded.getUserId());

		String rawKey = runPrefix + "cache:read:" + session.getSessionId();
		String rawBefore = templateA.opsForValue().get(rawKey);
		Assert.assertNotNull(rawBefore);
		Long ttlBefore = templateA.getExpire(rawKey);

		//实例B续期：只延长TTL，不重写值
		sessionManagerB.setExpireSeconds(60L);
		sessionManagerB.refreshAliveTime(session.getSessionId());

		String rawAfter = templateA.opsForValue().get(rawKey);
		Long ttlAfter = templateA.getExpire(rawKey);
		Assert.assertEquals("续期不应重写会话值", rawBefore, rawAfter);
		Assert.assertTrue("续期后TTL应被延长，before=" + ttlBefore + ", after=" + ttlAfter,
				ttlAfter != null && ttlAfter >= 55L);
		Assert.assertTrue("续期前TTL应接近15秒，实际=" + ttlBefore, ttlBefore > 0 && ttlBefore <= 15L);
		System.out.println("[redis-e2e] session key=" + rawKey + " ttlBefore=" + ttlBefore + " ttlAfter=" + ttlAfter
				+ " valueUnchanged=true");

		sessionManagerB.remove(session.getSessionId());
		Assert.assertNull(sessionManagerA.get(session.getSessionId()));
	}

	// ------------------------------------------------------------------
	// 6. 国际化缓存跨实例
	// ------------------------------------------------------------------

	@Test
	public void testLanguageCacheCrossInstance() {
		String cacheKey = "zh_CN.error.system";
		cacheA.set(cacheKey, "系统错误", 180L, CacheExpireMode.EXPIRE_AFTER_WRITE);
		Assert.assertEquals("系统错误", cacheB.get(cacheKey, String.class, 180L, CacheExpireMode.EXPIRE_AFTER_WRITE));

		String rawKey = runPrefix + "cache:write:" + cacheKey;
		Assert.assertNotNull(templateA.opsForValue().get(rawKey));
		System.out.println("[redis-e2e] i18n key=" + rawKey + " raw=" + templateA.opsForValue().get(rawKey)
				+ " ttl=" + templateA.getExpire(rawKey));
	}

	// ------------------------------------------------------------------
	// 7. 验证码 get + remove 跨实例（非原子消费窗口说明）
	// ------------------------------------------------------------------

	@Test
	public void testCaptchaGetThenRemoveAcrossInstances() {
		String captchaKey = "captcha:" + UUID.randomUUID().toString();
		cacheA.set(captchaKey, "1234", 300L, CacheExpireMode.EXPIRE_AFTER_WRITE);

		Assert.assertEquals("1234", cacheB.get(captchaKey, 300L, CacheExpireMode.EXPIRE_AFTER_WRITE));
		cacheB.remove(captchaKey, 300L, CacheExpireMode.EXPIRE_AFTER_WRITE);
		Assert.assertNull("remove后实例A也不应再读到", cacheA.get(captchaKey, 300L, CacheExpireMode.EXPIRE_AFTER_WRITE));
		System.out.println("[redis-e2e] captcha key=" + runPrefix + "cache:write:" + captchaKey);
	}

	// ------------------------------------------------------------------
	// 8. TTL到期
	// ------------------------------------------------------------------

	@Test
	public void testTtlExpiry() throws Exception {
		String key = "ttl-key";
		cacheA.set(key, "short-lived", 2L, CacheExpireMode.EXPIRE_AFTER_WRITE);
		String rawKey = runPrefix + "cache:write:" + key;
		Long ttlImmediately = templateA.getExpire(rawKey);
		Assert.assertTrue("写入后TTL应为2秒，实际=" + ttlImmediately, ttlImmediately != null && ttlImmediately > 0 && ttlImmediately <= 2L);

		Thread.sleep(2600L);
		Assert.assertNull("TTL到期后应读不到", cacheB.get(key, 2L, CacheExpireMode.EXPIRE_AFTER_WRITE));
		Long ttlAfterExpiry = templateA.getExpire(rawKey);
		Assert.assertEquals("TTL应返回-2（key不存在），实际=" + ttlAfterExpiry, -2L, ttlAfterExpiry.longValue());
		System.out.println("[redis-e2e] ttl key=" + rawKey + " ttlAtWrite=" + ttlImmediately + " ttlAfterExpiry=" + ttlAfterExpiry);
	}

	// ------------------------------------------------------------------
	// 9. 按前缀SCAN删除（clear）+ 哨兵不受影响
	// ------------------------------------------------------------------

	@Test
	public void testClearByPrefixKeepsOtherPrefixes() {
		//哨兵：不同前缀、同一Redis
		templateA.opsForValue().set(sentinelKey, "keep-me");
		//同一测试前缀下非cache命名空间的key，clear只应清理cache命名空间
		String otherKey = runPrefix + "other:keep";
		templateA.opsForValue().set(otherKey, "keep-me-too");

		cacheA.set("clear-a", "a", 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);
		cacheA.set("clear-b", "b", 60L, CacheExpireMode.EXPIRE_AFTER_WRITE);

		//clear(null)
		cacheA.clear();
		Assert.assertNull(cacheB.get("clear-a", 60L, CacheExpireMode.EXPIRE_AFTER_WRITE));

		//clear(ttl)在Redis模式等同于清理命名空间
		cacheA.set("clear-c", "c", 30L, CacheExpireMode.EXPIRE_AFTER_WRITE);
		cacheA.clear(30L);
		Assert.assertNull(cacheB.get("clear-c", 60L, CacheExpireMode.EXPIRE_AFTER_WRITE));

		Assert.assertEquals("哨兵key不应被清理", "keep-me", templateA.opsForValue().get(sentinelKey));
		Assert.assertEquals("非cache命名空间的key不应被清理", "keep-me-too", templateA.opsForValue().get(otherKey));

		long cacheResidual = countKeys(templateA, runPrefix + "cache:*");
		Assert.assertEquals("clear后cache命名空间应无残留", 0L, cacheResidual);
		System.out.println("[redis-e2e] clear residualCacheKeys=" + cacheResidual + " sentinelAlive=true");
	}

	// ------------------------------------------------------------------
	// 10. Redis故障降级
	// ------------------------------------------------------------------

	@Test
	public void testFallbackToLocalOnRedisFailure() throws Exception {
		RedisStateStore badStore = new RedisStateStore(templateBad, runPrefix);
		LocalStateStore localFallback = new LocalStateStore(new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE), runPrefix);
		FallbackStateStore fallbackStore = new FallbackStateStore(badStore, localFallback, 5L);

		Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(FallbackStateStore.class);
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
		try {
			fallbackStore.set("fallback:key", "local-value", BAD_CONNECT_TOKEN_TTL);
			Assert.assertEquals("Redis故障且fallback=true时应走本地存储，不抛出异常",
					"local-value", fallbackStore.get("fallback:key", BAD_CONNECT_TOKEN_TTL));
			fallbackStore.get("fallback:key", BAD_CONNECT_TOKEN_TTL);
			fallbackStore.get("fallback:key", BAD_CONNECT_TOKEN_TTL);

			long warns = appender.list.stream().filter(event -> event.getLevel().toString().equals("WARN")).count();
			Assert.assertTrue("故障期间WARN日志应限频（连续3次故障不超过1条），实际=" + warns, warns <= 1L);
			System.out.println("[redis-e2e] fallback=true localized=true warnCount=" + warns);
		} finally {
			logger.detachAppender(appender);
		}

		//fallback=false：直接使用Redis实现，异常必须向上抛出
		boolean thrown = false;
		Throwable cause = null;
		try {
			badStore.set("fallback:fail-fast", "v", BAD_CONNECT_TOKEN_TTL);
		} catch (Throwable e) {
			thrown = true;
			cause = e;
		}
		Assert.assertTrue("fallback=false时Redis故障应抛出异常", thrown);
		System.out.println("[redis-e2e] fallback=false exception=" + cause.getClass().getName() + ": " + cause.getMessage());
	}

	// ------------------------------------------------------------------
	// 工具方法
	// ------------------------------------------------------------------

	private static final class ConcurrencyResult {
		int success;
		int businessFailures;
		Throwable unexpected;
	}

	private ConcurrencyResult runConcurrently(List<Runnable> tasks) throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(tasks.size());
		AtomicInteger success = new AtomicInteger();
		AtomicInteger businessFailures = new AtomicInteger();
		AtomicReference<Throwable> unexpected = new AtomicReference<>();
		for (Runnable task : tasks) {
			pool.submit(() -> {
				try {
					start.await();
					task.run();
					success.incrementAndGet();
				} catch (BusinessException e) {
					businessFailures.incrementAndGet();
				} catch (Throwable e) {
					unexpected.compareAndSet(null, e);
				} finally {
					done.countDown();
				}
			});
		}
		start.countDown();
		done.await(60, TimeUnit.SECONDS);
		pool.shutdown();
		pool.awaitTermination(30, TimeUnit.SECONDS);

		ConcurrencyResult result = new ConcurrencyResult();
		result.success = success.get();
		result.businessFailures = businessFailures.get();
		result.unexpected = unexpected.get();
		return result;
	}

	private long countKeys(StringRedisTemplate template, String pattern) {
		Long count = template.execute((RedisCallback<Long>) connection -> {
			long total = 0L;
			Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(1000).build());
			try {
				while (cursor.hasNext()) {
					cursor.next();
					total++;
				}
			} finally {
				try {
					cursor.close();
				} catch (Exception ignored) {
				}
			}
			return total;
		});
		return count == null ? 0L : count;
	}

	private String configGet(StringRedisTemplate template, String param) {
		try {
			Object result = template.execute((RedisCallback<Object>) connection ->
					connection.execute("CONFIG", "GET".getBytes(), param.getBytes()));
			if (result == null) {
				return "null";
			}
			if (result instanceof byte[]) {
				//低版本Redis/Lettuce在此命令上可能返回单个字节数组，按顺序[参数名, 值]解析
				return new String((byte[]) result);
			}
			if (result instanceof List) {
				List<?> values = (List<?>) result;
				StringBuilder sb = new StringBuilder();
				for (Object value : values) {
					if (value instanceof byte[]) {
						if (sb.length() > 0) {
							sb.append(" -> ");
						}
						sb.append(new String((byte[]) value));
					}
				}
				return sb.length() == 0 ? String.valueOf(values) : sb.toString();
			}
			return String.valueOf(result);
		} catch (Exception e) {
			return "unsupported(" + e.getClass().getSimpleName() + ")";
		}
	}

	private String abbreviate(String raw) {
		if (raw == null || raw.length() <= 80) {
			return raw;
		}
		return raw.substring(0, 77) + "...";
	}
}
