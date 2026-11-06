package com.pddon.framework.easyapi.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * decorateObj对null对象的处理测试：null在类型判断之前已被拦截，
 * 直接交给装饰函数（内置处理器均原样返回null），不会抛出NullPointerException。
 */
public class BeanPropertyUtilNullDecorateTest {

	@Test
	public void testNullIsDelegatedToDecorator() throws Throwable {
		Object result = BeanPropertyUtil.decorateObj("field", null, null,
				(fieldName, data, annotations) -> {
					Assert.assertEquals("field", fieldName);
					Assert.assertNull(data);
					Assert.assertNull(annotations);
					return "decorated";
				});
		Assert.assertEquals("decorated", result);
	}

	@Test
	public void testNullWithIdentityDecoratorReturnsNull() throws Throwable {
		Assert.assertNull(BeanPropertyUtil.decorateObj("field", null, null,
				(fieldName, data, annotations) -> data));
	}

	@Test
	public void testNestedNullFieldIsDelegated() throws Throwable {
		NullFieldDto dto = new NullFieldDto();
		Object result = BeanPropertyUtil.decorateObj("req", dto, null,
				(fieldName, data, annotations) -> data);
		Assert.assertNull(((NullFieldDto) result).getValue());
	}

	static class NullFieldDto {
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
