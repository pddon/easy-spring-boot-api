package com.pddon.framework.easyapi.regression;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.pddon.framework.easyapi.utils.BeanPropertyUtil;

/**
 * 优化点2的隔离测试：getAllField 返回集合被外部修改后不得影响内部缓存与后续调用结果。
 */
public class BeanPropertyUtilFieldCacheIsolationTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testMutableReturnedListDoesNotPolluteCache() throws Exception {
        Sample sample = new Sample();
        List<Field> first = BeanPropertyUtil.getAllField(sample);
        Assert.assertFalse(first.isEmpty());

        ClassValue<List<Field>> cache = (ClassValue<List<Field>>) ReflectionTestUtils
                .getField(BeanPropertyUtil.class, "ALL_FIELD_CACHE");
        List<Field> cachedBefore = cache.get(Sample.class);
        List<String> snapshot = signatures(cachedBefore);

        // 外部对返回集合做 clear/add/remove
        Field foreign = String.class.getDeclaredField("value");
        first.clear();
        Assert.assertTrue(first.isEmpty());
        first.add(foreign);
        Assert.assertEquals(1, first.size());

        List<Field> second = BeanPropertyUtil.getAllField(sample);
        Assert.assertNotSame(first, second);
        Assert.assertEquals(snapshot, signatures(second));
        Assert.assertEquals(snapshot, signatures(cache.get(Sample.class)));

        // 再次 remove 不影响缓存
        second.remove(0);
        List<Field> third = BeanPropertyUtil.getAllField(sample);
        Assert.assertEquals(snapshot, signatures(third));
        Assert.assertEquals(snapshot, signatures(cache.get(Sample.class)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCachedFieldListIsUnmodifiableAndReflectsInheritance() throws Exception {
        ClassValue<List<Field>> cache = (ClassValue<List<Field>>) ReflectionTestUtils
                .getField(BeanPropertyUtil.class, "ALL_FIELD_CACHE");
        List<Field> cached = cache.get(Child.class);
        Assert.assertEquals(Arrays.asList("childField", "parentField", "baseField"), names(cached));
        try {
            cached.add(Child.class.getDeclaredField("childField"));
            Assert.fail("内部缓存集合应为不可变集合");
        } catch (UnsupportedOperationException expected) {
            // 预期
        }
    }

    private static List<String> signatures(List<Field> fields) {
        List<String> result = new ArrayList<>();
        for (Field field : fields) {
            result.add(field.getDeclaringClass().getName() + "#" + field.getName());
        }
        return result;
    }

    private static List<String> names(List<Field> fields) {
        List<String> result = new ArrayList<>();
        for (Field field : fields) {
            result.add(field.getName());
        }
        return result;
    }

    public static class Sample {
        private String sampleField;
        private int number;
    }

    public static class Base {
        private String baseField;
    }

    public static class Parent extends Base {
        private String parentField;
    }

    public static class Child extends Parent {
        private String childField;
    }
}
