package com.pddon.framework.easyapi.regression;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.pddon.framework.easyapi.InvokeApiLogManager;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiInfo;
import com.pddon.framework.easyapi.dto.ApiInvokeLog;
import com.pddon.framework.easyapi.dto.ApiRequestParameter;
import com.pddon.framework.easyapi.interceptor.impl.ApiLogInterceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 优化点4的对照测试：ApiLogInterceptor 旧实现 vs 生产实现，日志对象逐字段一致、save 次数不变。
 */
public class ApiLogInterceptorParityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testSavedLogObjectMatchesLegacyFieldByField() throws Throwable {
        InvokeApiLogManager legacyManager = mock(InvokeApiLogManager.class);
        InvokeApiLogManager producedManager = mock(InvokeApiLogManager.class);
        ApiLogInterceptor interceptor = newInterceptor(producedManager);

        List<ApiRequestParameter> params = prepareContext();
        LegacyApiLogInterceptor.preInvoke(legacyManager, MAPPER, params);
        interceptor.preInvoke(params, dummyMethod(), Object.class);

        ArgumentCaptor<ApiInvokeLog> legacyCaptor = ArgumentCaptor.forClass(ApiInvokeLog.class);
        ArgumentCaptor<ApiInvokeLog> producedCaptor = ArgumentCaptor.forClass(ApiInvokeLog.class);
        verify(legacyManager, times(1)).save(legacyCaptor.capture());
        verify(producedManager, times(1)).save(producedCaptor.capture());

        assertSameFields(legacyCaptor.getValue(), producedCaptor.getValue());
        Assert.assertNotNull(producedCaptor.getValue().getSystemParams());
        Assert.assertEquals("trace-123", producedCaptor.getValue().getSystemParams().get("traceId"));
        Assert.assertEquals("unit-test", producedCaptor.getValue().getSystemParams().get("source"));
    }

    @Test
    public void testInfoDisabledStillSavesExactlyOnceWithSameContent() throws Throwable {
        Logger logger = (Logger) LoggerFactory.getLogger(ApiLogInterceptor.class);
        Level originalLevel = logger.getLevel();
        InvokeApiLogManager legacyManager = mock(InvokeApiLogManager.class);
        InvokeApiLogManager producedManager = mock(InvokeApiLogManager.class);
        try {
            logger.setLevel(Level.WARN);
            ApiLogInterceptor interceptor = newInterceptor(producedManager);
            List<ApiRequestParameter> params = prepareContext();
            LegacyApiLogInterceptor.preInvoke(legacyManager, MAPPER, params);
            interceptor.preInvoke(params, dummyMethod(), Object.class);

            ArgumentCaptor<ApiInvokeLog> legacyCaptor = ArgumentCaptor.forClass(ApiInvokeLog.class);
            ArgumentCaptor<ApiInvokeLog> producedCaptor = ArgumentCaptor.forClass(ApiInvokeLog.class);
            verify(legacyManager, times(1)).save(legacyCaptor.capture());
            verify(producedManager, times(1)).save(producedCaptor.capture());
            assertSameFields(legacyCaptor.getValue(), producedCaptor.getValue());
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    @Test
    public void testRequestSnapshotStringMatchesLegacy() throws Throwable {
        InvokeApiLogManager manager = mock(InvokeApiLogManager.class);
        ApiLogInterceptor interceptor = newInterceptor(manager);
        List<ApiRequestParameter> params = prepareContext();

        String produced = interceptor.objectArrToJSONString(params);
        String legacy = legacyObjectArrToJSONString(params);
        Assert.assertEquals(legacy, produced);
        Assert.assertTrue(produced.contains("sample-request"));
        Assert.assertTrue(produced.contains("upload.txt"));
    }

    @Test
    public void testResponseSnapshotWriterMatchesLegacy() throws Throwable {
        ApiLogInterceptor interceptor = newInterceptor(mock(InvokeApiLogManager.class));
        ObjectWriter producedWriter = (ObjectWriter) ReflectionTestUtils.invokeMethod(interceptor, "prettyWriter");
        ObjectWriter legacyWriter = MAPPER.writerWithDefaultPrettyPrinter();
        SampleResponse response = new SampleResponse("ok", 100);
        Assert.assertEquals(legacyWriter.writeValueAsString(response), producedWriter.writeValueAsString(response));
    }

    private static ApiLogInterceptor newInterceptor(InvokeApiLogManager manager) {
        ApiLogInterceptor interceptor = new ApiLogInterceptor();
        ReflectionTestUtils.setField(interceptor, "invokeApiLogManager", manager);
        ReflectionTestUtils.setField(interceptor, "objectMapper", MAPPER);
        return interceptor;
    }

    private static List<ApiRequestParameter> prepareContext() {
        RequestContext context = RequestContext.getContext();
        context.clear();
        context.setAttachment("requestId", "trace-123");
        context.setAttachment("appId", "app-1");
        context.setAttachment("userId", "user-1");
        context.setAttachment("sign", "sign-abc");
        context.setAttachment("clientIp", "127.0.0.1");
        context.setAttachment("locale", "zh_CN");
        context.setObjectAttachment("timestamp", 1600000000000L);
        context.setObjectAttachment("flag", Boolean.TRUE);
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("x", 1);
        nested.put("y", "nested-value");
        context.setObjectAttachment("nested", nested);
        context.setAttachment("listValue", "a,b,c");
        Map<String, Object> systemParams = new LinkedHashMap<>();
        systemParams.put("traceId", "trace-123");
        systemParams.put("source", "unit-test");
        context.setObjectAttachment("systemParams", systemParams);

        context.setApiInfo(new ApiInfo()
                .setApiName("Demo::handle")
                .setApiUri("/1.0/demo")
                .setApiMethod("POST")
                .setApiVersion("1.0")
                .setResponse(new SampleResponse("resp", 10)));

        List<ApiRequestParameter> params = new ArrayList<>();
        ApiRequestParameter first = new ApiRequestParameter();
        first.setIndex(0);
        first.setParamName("sample");
        first.setParam(new SampleRequest("sample-request", 5));
        params.add(first);
        ApiRequestParameter second = new ApiRequestParameter();
        second.setIndex(1);
        second.setParamName("file");
        second.setParam(new MockMultipartFile("file", "upload.txt", "text/plain", "data".getBytes()));
        params.add(second);
        return params;
    }

    private static Method dummyMethod() throws NoSuchMethodException {
        return ApiLogInterceptorParityTest.class.getDeclaredMethod("dummy", String.class, int.class);
    }

    @SuppressWarnings("unused")
    private void dummy(String first, int second) {
    }

    private static String legacyObjectArrToJSONString(List<ApiRequestParameter> params) throws Exception {
        StringBuilder buffer = new StringBuilder();
        for (ApiRequestParameter param : params) {
            if (param.getParam() instanceof MockMultipartFile) {
                buffer.append(((MockMultipartFile) param.getParam()).getOriginalFilename()).append(',');
                continue;
            }
            buffer.append(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(param.getParam())).append(',');
        }
        return buffer.length() > 1 ? buffer.substring(0, buffer.length() - 1) : buffer.toString();
    }

    private static void assertSameFields(ApiInvokeLog legacy, ApiInvokeLog produced) throws Exception {
        Assert.assertNotNull(legacy);
        Assert.assertNotNull(produced);
        for (Field field : ApiInvokeLog.class.getDeclaredFields()) {
            if (field.isSynthetic()) {
                continue;
            }
            field.setAccessible(true);
            Assert.assertEquals("字段不一致:" + field.getName(), field.get(legacy), field.get(produced));
        }
    }

    public static class SampleResponse {
        private String status;
        private int code;

        public SampleResponse() {
        }

        SampleResponse(String status, int code) {
            this.status = status;
            this.code = code;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    public static class SampleRequest {
        private String name;
        private int qty;

        public SampleRequest() {
        }

        SampleRequest(String name, int qty) {
            this.name = name;
            this.qty = qty;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }
    }
}
