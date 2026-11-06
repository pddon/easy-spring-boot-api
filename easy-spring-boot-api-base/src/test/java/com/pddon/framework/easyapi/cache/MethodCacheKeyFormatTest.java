package com.pddon.framework.easyapi.cache;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.pddon.framework.easyapi.consts.CacheKeyMode;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;

/**
 * 方法缓存自动参数key的格式测试：
 * 1. 默认（旧格式）与改造前逐字符一致；
 * 2. 旧格式的三类碰撞样例（"-"拼接、null参数省略、sign子串误剔除）；
 * 3. 新格式（easyapi.method-cache.auto-key-v2=true）消除三类碰撞，且结果稳定。
 */
public class MethodCacheKeyFormatTest {

	private static final String PREFIX = "Demo:Key";

	private static final String[] QUERY_PARAMS = {"first", "second"};
	private static final String[] SINGLE_PARAMS = {"q"};

	private MethodCacheManager manager;
	private Method queryMethod;
	private Method singleMethod;

	@Before
	public void setUp() throws Exception {
		manager = new MethodCacheManager();
		ReflectionTestUtils.setField(manager, "systemParameterRenameProperties", new SystemParameterRenameProperties());
		queryMethod = KeyFormatSample.class.getDeclaredMethod("query", Object.class, Object.class);
		singleMethod = KeyFormatSample.class.getDeclaredMethod("single", Object.class);
	}

	@Test
	public void testLegacyFormatUnchanged() {
		Assert.assertEquals("Demo:Key:a-b", key(false, new Object[]{"a", "b"}, QUERY_PARAMS, queryMethod));
		Assert.assertEquals("Demo:Key:", key(false, new Object[]{null, null}, QUERY_PARAMS, queryMethod));
		Assert.assertEquals("Demo:Key:", key(false, new Object[0], QUERY_PARAMS, queryMethod));
	}

	@Test
	public void testLegacyJoiningCollisionExists() {
		//旧格式把参数值直接以"-"拼接：("x-y","z")与("x","y-z")得到同一个key
		Assert.assertEquals(
				key(false, new Object[]{"x-y", "z"}, QUERY_PARAMS, queryMethod),
				key(false, new Object[]{"x", "y-z"}, QUERY_PARAMS, queryMethod));
	}

	@Test
	public void testV2SeparatesJoiningCollision() {
		Assert.assertNotEquals(
				key(true, new Object[]{"x-y", "z"}, QUERY_PARAMS, queryMethod),
				key(true, new Object[]{"x", "y-z"}, QUERY_PARAMS, queryMethod));
	}

	@Test
	public void testLegacyNullCollisionExists() {
		//旧格式省略null参数：("x",null)与(null,"x")得到同一个key
		Assert.assertEquals(
				key(false, new Object[]{null, "x"}, QUERY_PARAMS, queryMethod),
				key(false, new Object[]{"x", null}, QUERY_PARAMS, queryMethod));
	}

	@Test
	public void testV2SeparatesNullArguments() {
		Assert.assertNotEquals(
				key(true, new Object[]{null, "x"}, QUERY_PARAMS, queryMethod),
				key(true, new Object[]{"x", null}, QUERY_PARAMS, queryMethod));
	}

	@Test
	public void testExactFieldFilterKeepsBusinessFieldContainingSign() {
		//精确匹配字段名后，design/signal不再被当作sign剔除（新旧格式都生效）
		Assert.assertTrue(key(false, new Object[]{new DesignDto("d1")}, SINGLE_PARAMS, singleMethod).contains("d1"));
		Assert.assertTrue(key(false, new Object[]{new SignalDto("s1")}, SINGLE_PARAMS, singleMethod).contains("s1"));
		Assert.assertTrue(key(true, new Object[]{new DesignDto("d1")}, SINGLE_PARAMS, singleMethod).contains("q.design=d1"));
		Assert.assertTrue(key(true, new Object[]{new SignalDto("s1")}, SINGLE_PARAMS, singleMethod).contains("q.signal=s1"));
	}

	@Test
	public void testV2StillExcludesExactSystemParamField() {
		Assert.assertFalse(key(true, new Object[]{new SignDto("sig")}, SINGLE_PARAMS, singleMethod).contains("sign"));
		Assert.assertFalse(key(true, new Object[]{new AppIdDto("app1")}, SINGLE_PARAMS, singleMethod).contains("appId"));
	}

	@Test
	public void testV2DeterministicAndDistinctFromLegacy() {
		Assert.assertEquals(
				key(true, new Object[]{"a", "b"}, QUERY_PARAMS, queryMethod),
				key(true, new Object[]{"a", "b"}, QUERY_PARAMS, queryMethod));
		Assert.assertNotEquals(
				key(false, new Object[]{"a", "b"}, QUERY_PARAMS, queryMethod),
				key(true, new Object[]{"a", "b"}, QUERY_PARAMS, queryMethod));
	}

	private String key(boolean v2, Object[] args, String[] parameterNames, Method method) {
		return manager.getCacheKey(PREFIX, CacheKeyMode.AUTO_BY_PARAMS, null,
				parameterNames, args, KeyFormatSample.class, method, v2);
	}

	static class KeyFormatSample {
		public Object query(Object first, Object second) {
			return null;
		}

		public Object single(Object q) {
			return null;
		}
	}

	static class DesignDto {
		private String design;

		DesignDto(String design) {
			this.design = design;
		}

		public String getDesign() {
			return design;
		}

		public void setDesign(String design) {
			this.design = design;
		}
	}

	static class SignalDto {
		private String signal;

		SignalDto(String signal) {
			this.signal = signal;
		}

		public String getSignal() {
			return signal;
		}

		public void setSignal(String signal) {
			this.signal = signal;
		}
	}

	static class SignDto {
		private String sign;

		SignDto(String sign) {
			this.sign = sign;
		}

		public String getSign() {
			return sign;
		}

		public void setSign(String sign) {
			this.sign = sign;
		}
	}

	static class AppIdDto {
		private String appId;

		AppIdDto(String appId) {
			this.appId = appId;
		}

		public String getAppId() {
			return appId;
		}

		public void setAppId(String appId) {
			this.appId = appId;
		}
	}
}
