package com.pddon.framework.easyapi.regression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.AbstractList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.interceptor.impl.ApiLogInterceptor;

/**
 * 优化点1的验证：
 * 1）请求参数/响应日志的截断结果与旧实现逐字符一致；
 * 2）超长内容在达到上限时立即停止序列化，不再构造完整JSON字符串；
 * 3）请求参数日志在一次请求内只生成一次（请求阶段生成，响应阶段复用）。
 */
public class ApiLogInterceptorMemoryTest {

    private static final int MAX_LOG_SIZE = 1024;
    private static final String REQUEST_TAIL = "//...";
    private static final String RESPONSE_TAIL = "\n//...\n}";

    private ApiLogInterceptor interceptor;

    @Before
    public void setUp() {
        interceptor = new ApiLogInterceptor();
        ReflectionTestUtils.setField(interceptor, "objectMapper", new ObjectMapper());
    }

    @Test
    public void testShortRequestLogMatchesLegacyExactly() {
        List<ApiRequestParameter> params = params(
                param(0, "name", "abc"),
                param(1, "count", 123),
                param(2, "nullValue", null));
        Assert.assertEquals(legacyRequestLog(params), objectArrToLogString(params));
    }

    @Test
    public void testMultipartRequestLogMatchesLegacyExactly() {
        List<ApiRequestParameter> params = params(
                param(0, "file", new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes())),
                param(1, "name", "abc"));
        Assert.assertEquals(legacyRequestLog(params), objectArrToLogString(params));
    }

    @Test
    public void testLongRequestLogTruncatesIdenticallyAndSerializesLess() {
        //每个元素约200字符，5000个元素完整序列化约1MB；旧实现必须先分配整份字符串
        CountingStringList legacyList = new CountingStringList(5000, 200);
        List<ApiRequestParameter> legacyParams = params(
                param(0, "prefix", "x"),
                param(1, "items", legacyList));
        String expected = legacyRequestLog(legacyParams);
        //旧实现把5000个元素全部遍历/序列化
        Assert.assertEquals(5000, legacyList.iterations);

        CountingStringList producedList = new CountingStringList(5000, 200);
        List<ApiRequestParameter> producedParams = params(
                param(0, "prefix", "x"),
                param(1, "items", producedList));
        String produced = objectArrToLogString(producedParams);
        Assert.assertEquals(expected, produced);
        //Jackson的WriterBasedJsonGenerator内部有约4000字符的缓冲，写满后才落到有界Writer，
        //因此序列化在约4000字符处中断（约20个元素），而不是遍历全部5000个元素
        Assert.assertTrue("应在上限处提前停止，实际遍历=" + producedList.iterations,
                producedList.iterations <= 25);
        Assert.assertEquals(MAX_LOG_SIZE + REQUEST_TAIL.length(), produced.length());
    }

    @Test
    public void testLongResponseLogTruncatesIdenticallyAndSerializesLess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CountingStringList legacyList = new CountingStringList(5000, 200);
        String full = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(legacyList);
        Assert.assertEquals(5000, legacyList.iterations);
        String expected = full.length() > MAX_LOG_SIZE
                ? full.substring(0, MAX_LOG_SIZE) + RESPONSE_TAIL : full;

        CountingStringList producedList = new CountingStringList(5000, 200);
        String produced = (String) ReflectionTestUtils.invokeMethod(
                interceptor, "boundedJsonToString", producedList, MAX_LOG_SIZE, RESPONSE_TAIL);
        Assert.assertEquals(expected, produced);
        //同上：序列化在约4000字符（约20个元素）处中断，最终字符串从约1MB降为1025字符
        Assert.assertTrue("响应日志应在达到上限时停止序列化，实际遍历=" + producedList.iterations,
                producedList.iterations <= 25);
        Assert.assertTrue("最终日志长度应受上限约束", produced.length() <= MAX_LOG_SIZE + RESPONSE_TAIL.length());
    }

    @Test
    public void testRequestLogContentIsGeneratedOncePerRequest() {
        RequestContext context = RequestContext.getContext();
        context.clear();
        List<ApiRequestParameter> params = params(param(0, "name", "first"));
        String first = (String) ReflectionTestUtils.invokeMethod(interceptor, "requestLogContent", params);
        Assert.assertNotNull(first);
        Assert.assertEquals(first, context.getRequestLogContent());

        //同一请求内给列表追加参数后再次获取，应命中文档快照而不是重新序列化
        params.add(param(1, "other", "second"));
        String second = (String) ReflectionTestUtils.invokeMethod(interceptor, "requestLogContent", params);
        Assert.assertEquals(first, second);
        context.clear();
    }

    private String objectArrToLogString(List<ApiRequestParameter> params) {
        return (String) ReflectionTestUtils.invokeMethod(interceptor, "objectArrToLogString", params);
    }

    /**
     * 旧实现：先拼接完整JSON，再按长度截断
     */
    private String legacyRequestLog(List<ApiRequestParameter> params) {
        String full = interceptor.objectArrToJSONString(params);
        if (full != null && full.length() > MAX_LOG_SIZE) {
            return full.substring(0, MAX_LOG_SIZE) + REQUEST_TAIL;
        }
        return full;
    }

    private static List<ApiRequestParameter> params(ApiRequestParameter... items) {
        List<ApiRequestParameter> list = new ArrayList<>();
        for (ApiRequestParameter item : items) {
            list.add(item);
        }
        return list;
    }

    private static ApiRequestParameter param(int index, String name, Object value) {
        ApiRequestParameter param = new ApiRequestParameter();
        param.setIndex(index);
        param.setParamName(name);
        param.setParam(value);
        return param;
    }

    /**
     * 统计被序列化的元素个数，用于量化"提前停止序列化"的收益
     */
    static class CountingStringList extends AbstractList<String> implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final int size;
        private final int length;
        volatile int iterations;

        CountingStringList(int size, int length) {
            this.size = size;
            this.length = length;
        }

        @Override
        public String get(int index) {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append('x');
            }
            return sb.toString();
        }

        @Override
        public int size() {
            return size;
        }

        /**
         * 避免异常包装/日志打印调用AbstractCollection.toString时遍历全部元素，干扰计数
         */
        @Override
        public String toString() {
            return "CountingStringList(size=" + size + ")";
        }

        @Override
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                private int cursor;

                @Override
                public boolean hasNext() {
                    return cursor < size;
                }

                @Override
                public String next() {
                    iterations++;
                    return get(cursor++);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
