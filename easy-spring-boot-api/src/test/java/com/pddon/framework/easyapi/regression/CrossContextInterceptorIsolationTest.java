package com.pddon.framework.easyapi.regression;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import com.pddon.framework.easyapi.aspect.ApiResponseAspector;
import com.pddon.framework.easyapi.dataformat.AbstractFieldDataFormatHandler;
import com.pddon.framework.easyapi.dataformat.FieldDataFormatHandler;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.interceptor.AbstractApiMethodInterceptor;
import com.pddon.framework.easyapi.interceptor.ApiInvokeMethodInterceptorManager;
import com.pddon.framework.easyapi.interceptor.ApiMethodInterceptor;
import com.pddon.framework.easyapi.interceptor.impl.ApiDataFormatInterceptor;
import com.pddon.framework.easyapi.response.enhance.AbstractResponseEnhanceHandler;
import com.pddon.framework.easyapi.response.enhance.ResponseEnhanceHandler;

/**
 * 静态注册集合的上下文隔离测试：
 * 1. 同一JVM中两个上下文各自持有自己的拦截器/处理器，第二个上下文的链不包含第一个上下文的实例；
 * 2. 上下文关闭后注册表中的实例被清理；
 * 3. 单上下文下的链与改造前一致（按order排序，包含该上下文全部实例）；
 * 4. 业务通过静态addInterceptor手工注册的实例仍然生效。
 */
@SuppressWarnings("unchecked")
public class CrossContextInterceptorIsolationTest {

	@Test
	public void testInterceptorsAreIsolatedPerContext() {
		AnnotationConfigApplicationContext ctx1 = new AnnotationConfigApplicationContext(ContextOneConfig.class);
		AnnotationConfigApplicationContext ctx2 = new AnnotationConfigApplicationContext(ContextTwoConfig.class);
		try {
			List<ApiMethodInterceptor> list1 = interceptorList(ctx1);
			List<ApiMethodInterceptor> list2 = interceptorList(ctx2);
			Assert.assertNotNull(list1);
			Assert.assertNotNull(list2);

			Object ctx1Bean = ctx1.getBean(OrderTwentyInterceptor.class);
			Assert.assertFalse(list2.contains(ctx1Bean));
			Assert.assertFalse(legacyInterceptors().contains(ctx1Bean));

			//单上下文行为：包含该上下文全部实例，并按order稳定排序
			Assert.assertEquals(2, list1.size());
			Assert.assertEquals(10, list1.get(0).order());
			Assert.assertEquals(20, list1.get(1).order());
			Assert.assertTrue(list1.contains(ctx1Bean));

			Assert.assertEquals(1, list2.size());
			Assert.assertTrue(list2.contains(ctx2.getBean(OrderThirtyInterceptor.class)));
		} finally {
			ctx1.close();
			ctx2.close();
		}
		//上下文关闭后不再持有该上下文的实例
		Assert.assertNull(interceptorListRaw(ctx1));
	}

	@Test
	public void testManagerResolvesCurrentContextChainOnly() {
		AnnotationConfigApplicationContext ctx1 = new AnnotationConfigApplicationContext(ContextOneConfig.class);
		AnnotationConfigApplicationContext ctx2 = new AnnotationConfigApplicationContext(ContextTwoConfig.class);
		try {
			ApiInvokeMethodInterceptorManager manager1 = new ApiInvokeMethodInterceptorManager();
			ReflectionTestUtils.setField(manager1, "applicationContext", ctx1);
			List<ApiMethodInterceptor> resolved1 = (List<ApiMethodInterceptor>) ReflectionTestUtils
					.invokeMethod(manager1, "resolveInterceptors");

			ApiInvokeMethodInterceptorManager manager2 = new ApiInvokeMethodInterceptorManager();
			ReflectionTestUtils.setField(manager2, "applicationContext", ctx2);
			List<ApiMethodInterceptor> resolved2 = (List<ApiMethodInterceptor>) ReflectionTestUtils
					.invokeMethod(manager2, "resolveInterceptors");

			Object ctx1Bean = ctx1.getBean(OrderTwentyInterceptor.class);
			Assert.assertTrue(resolved1.contains(ctx1Bean));
			Assert.assertFalse(resolved2.contains(ctx1Bean));
			Assert.assertTrue(resolved2.contains(ctx2.getBean(OrderThirtyInterceptor.class)));
		} finally {
			ctx1.close();
			ctx2.close();
		}
	}

	@Test
	public void testDataFormatHandlersAreIsolatedPerContext() {
		AnnotationConfigApplicationContext ctx1 = new AnnotationConfigApplicationContext(ContextOneConfig.class);
		AnnotationConfigApplicationContext ctx2 = new AnnotationConfigApplicationContext(ContextTwoConfig.class);
		try {
			List<FieldDataFormatHandler> list1 = (List<FieldDataFormatHandler>) ReflectionTestUtils
					.invokeMethod(ApiDataFormatInterceptor.class, "getContextHandlers", ctx1);
			List<FieldDataFormatHandler> list2 = (List<FieldDataFormatHandler>) ReflectionTestUtils
					.invokeMethod(ApiDataFormatInterceptor.class, "getContextHandlers", ctx2);
			Assert.assertNotNull(list1);
			Assert.assertNotNull(list2);
			Object ctx1Handler = ctx1.getBean(ContextOneFormatHandler.class);
			Assert.assertFalse(list2.contains(ctx1Handler));
			Assert.assertTrue(list1.contains(ctx1Handler));
			Assert.assertFalse(legacyHandlers().contains(ctx1Handler));
		} finally {
			ctx1.close();
			ctx2.close();
		}
		Assert.assertNull((List<FieldDataFormatHandler>) ReflectionTestUtils
				.invokeMethod(ApiDataFormatInterceptor.class, "getContextHandlers", ctx1));
	}

	@Test
	public void testResponseEnhanceHandlersAreIsolatedPerContext() {
		AnnotationConfigApplicationContext ctx1 = new AnnotationConfigApplicationContext(ContextOneConfig.class);
		AnnotationConfigApplicationContext ctx2 = new AnnotationConfigApplicationContext(ContextTwoConfig.class);
		try {
			List<ResponseEnhanceHandler> list1 = (List<ResponseEnhanceHandler>) ReflectionTestUtils
					.invokeMethod(ApiResponseAspector.class, "getContextHandlers", ctx1);
			List<ResponseEnhanceHandler> list2 = (List<ResponseEnhanceHandler>) ReflectionTestUtils
					.invokeMethod(ApiResponseAspector.class, "getContextHandlers", ctx2);
			Assert.assertNotNull(list1);
			Assert.assertNotNull(list2);
			Object ctx1Handler = ctx1.getBean(ContextOneResponseHandler.class);
			Assert.assertFalse(list2.contains(ctx1Handler));
			Assert.assertTrue(list1.contains(ctx1Handler));
		} finally {
			ctx1.close();
			ctx2.close();
		}
	}

	@Test
	public void testManualRegistrationStillWorks() {
		ManualInterceptor manual = new ManualInterceptor();
		ApiInvokeMethodInterceptorManager.addInterceptor(manual);
		try {
			ApiInvokeMethodInterceptorManager manager = new ApiInvokeMethodInterceptorManager();
			List<ApiMethodInterceptor> resolved = (List<ApiMethodInterceptor>) ReflectionTestUtils
					.invokeMethod(manager, "resolveInterceptors");
			Assert.assertNotNull(resolved);
			Assert.assertTrue(resolved.contains(manual));
		} finally {
			legacyInterceptors().remove(manual);
		}
	}

	private List<ApiMethodInterceptor> interceptorList(AnnotationConfigApplicationContext context) {
		return (List<ApiMethodInterceptor>) interceptorListRaw(context);
	}

	private Object interceptorListRaw(AnnotationConfigApplicationContext context) {
		return ReflectionTestUtils.invokeMethod(ApiInvokeMethodInterceptorManager.class, "getContextInterceptors", context);
	}

	private List<ApiMethodInterceptor> legacyInterceptors() {
		return (List<ApiMethodInterceptor>) ReflectionTestUtils.getField(ApiInvokeMethodInterceptorManager.class, "interceptors");
	}

	private List<FieldDataFormatHandler> legacyHandlers() {
		return (List<FieldDataFormatHandler>) ReflectionTestUtils.getField(ApiDataFormatInterceptor.class, "handlers");
	}

	@Configuration
	static class ContextOneConfig {
		@Bean
		public OrderTwentyInterceptor orderTwentyInterceptor() {
			return new OrderTwentyInterceptor();
		}

		@Bean
		public OrderTenInterceptor orderTenInterceptor() {
			return new OrderTenInterceptor();
		}

		@Bean
		public ContextOneFormatHandler contextOneFormatHandler() {
			return new ContextOneFormatHandler();
		}

		@Bean
		public ContextOneResponseHandler contextOneResponseHandler() {
			return new ContextOneResponseHandler();
		}
	}

	@Configuration
	static class ContextTwoConfig {
		@Bean
		public OrderThirtyInterceptor orderThirtyInterceptor() {
			return new OrderThirtyInterceptor();
		}

		@Bean
		public ContextTwoFormatHandler contextTwoFormatHandler() {
			return new ContextTwoFormatHandler();
		}

		@Bean
		public ContextTwoResponseHandler contextTwoResponseHandler() {
			return new ContextTwoResponseHandler();
		}
	}

	public static class OrderTenInterceptor extends AbstractApiMethodInterceptor {
		@Override
		public int order() {
			return 10;
		}

		@Override
		public void preInvoke(List<ApiRequestParameter> requestParams, java.lang.reflect.Method method, Class<?> targetClass) {
		}

		@Override
		public Object afterInvoke(List<ApiRequestParameter> requestParams, Object resp, java.lang.reflect.Method method, Class<?> targetClass) {
			return resp;
		}
	}

	public static class OrderTwentyInterceptor extends OrderTenInterceptor {
		@Override
		public int order() {
			return 20;
		}
	}

	public static class OrderThirtyInterceptor extends OrderTenInterceptor {
		@Override
		public int order() {
			return 30;
		}
	}

	public static class ManualInterceptor implements ApiMethodInterceptor {
		@Override
		public int order() {
			return 40;
		}

		@Override
		public void preInvoke(List<ApiRequestParameter> requestParams, java.lang.reflect.Method method, Class<?> targetClass) {
		}

		@Override
		public Object afterInvoke(List<ApiRequestParameter> requestParams, Object resp, java.lang.reflect.Method method, Class<?> targetClass) {
			return resp;
		}
	}

	public static class ContextOneFormatHandler extends AbstractFieldDataFormatHandler {
		@Override
		public int order() {
			return 1;
		}

		@Override
		public boolean handleRequest() {
			return true;
		}

		@Override
		public boolean handleResponse() {
			return false;
		}

		@Override
		public Object handle(String fieldName, Object data, Map<Class<?>, Annotation> annotations) {
			return data;
		}
	}

	public static class ContextTwoFormatHandler extends ContextOneFormatHandler {
	}

	public static class ContextOneResponseHandler extends AbstractResponseEnhanceHandler {
		@Override
		public int order() {
			return 1;
		}

		@Override
		public Object enhance(Object resp) {
			return resp;
		}
	}

	public static class ContextTwoResponseHandler extends ContextOneResponseHandler {
	}
}
