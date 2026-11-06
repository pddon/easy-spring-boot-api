package com.pddon.framework.easyapi.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;
import com.pddon.framework.easyapi.client.consts.HttpMethod;
import com.pddon.framework.easyapi.controller.request.BaseRequest;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.EncryptUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * 客户端端到端测试：使用内嵌HttpServer（随机端口）+ 测试专用Spring上下文，
 * 无需部署demo服务、无需固定端口，验证：
 * 1、GET请求系统参数、加签、响应解析、响应验签全链路；
 * 2、服务端返回业务错误码时客户端能正确解析；
 * 3、请求签名与服务端计算结果一致。
 */
public class ApiClientTest {

    private static final String SECRET = "test-secret";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static HttpServer server;
    private static ConfigurableApplicationContext context;
    private static ApiClient apiClient;
    private static int port;

    @BeforeClass
    public static void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ApiClientTest::handle);
        server.start();
        port = server.getAddress().getPort();

        String baseUrl = "http://127.0.0.1:" + port + "/";
        context = new SpringApplicationBuilder(StartTestDemoConfig.class)
                .web(WebApplicationType.NONE)
                .properties(
                        "easyapi.client.app.baseUrl=" + baseUrl,
                        "easyapi.client.app.appId=webApp",
                        "easyapi.client.app.channelId=default",
                        "easyapi.client.app.locale=zh_CN",
                        "easyapi.client.app.secret=" + SECRET)
                .run();
        apiClient = context.getBean(ApiClient.class);
        ApplicationConfig config = context.getBean(ApplicationConfig.class);
        Assert.assertEquals(baseUrl, config.getBaseUrl());
    }

    @AfterClass
    public static void tearDown() {
        if (context != null) {
            context.close();
        }
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void testSignedGetRequestAndResponse() {
        SimpleRequest request = new SimpleRequest();
        request.setOrderId("client-test-001");

        DefaultResponseWrapper<Map> response = apiClient.executeRequest(
                ApiInfo.builder()
                        .apiName("system/errorList")
                        .method(HttpMethod.GET)
                        .needSign(true)
                        .needSignResult(true)
                        .build(),
                request, Map.class, new HashMap<>());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.isSuccess());
        Map data = response.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals("client-test-001", data.get("orderId"));
        Assert.assertEquals("SUCCESS", data.get("status"));
    }

    @Test
    public void testServerErrorCodeIsParsed() {
        SimpleRequest request = new SimpleRequest();
        request.setOrderId("client-test-002");

        DefaultResponseWrapper<Map> response = apiClient.executeRequest(
                ApiInfo.builder()
                        .apiName("error/mock")
                        .method(HttpMethod.GET)
                        .needSign(false)
                        .build(),
                request, Map.class, new HashMap<>());

        Assert.assertNotNull(response);
        Assert.assertFalse(response.isSuccess());
        Assert.assertEquals(Integer.valueOf(1001), response.getCode());
        Assert.assertEquals("业务处理失败", response.getMsg());
    }

    /**
     * 模拟服务端：解析参数、校验请求签名、返回带签名的响应
     */
    private static void handle(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> params = new HashMap<>();
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                parseQuery(exchange.getRequestURI().getRawQuery(), params);
            } else {
                String body = readBody(exchange.getRequestBody());
                if (body != null && !body.isEmpty()) {
                    params.putAll(MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {
                    }));
                }
            }

            if (exchange.getRequestURI().getPath().endsWith("/error/mock")) {
                writeJson(exchange, errorResponse());
                return;
            }

            if (params.get("sign") != null && !verifyRequestSign(params)) {
                writeJson(exchange, errorResponse());
                return;
            }
            writeJson(exchange, signedSuccessResponse(params));
        } catch (Exception e) {
            writeJson(exchange, errorResponse());
        } finally {
            exchange.close();
        }
    }

    private static void parseQuery(String rawQuery, Map<String, Object> params) throws IOException {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return;
        }
        for (String pair : rawQuery.split("&")) {
            int index = pair.indexOf('=');
            if (index < 0) {
                continue;
            }
            String key = URLDecoder.decode(pair.substring(0, index), "UTF-8");
            String value = URLDecoder.decode(pair.substring(index + 1), "UTF-8");
            params.put(key, value);
        }
    }

    private static String readBody(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static boolean verifyRequestSign(Map<String, Object> params) {
        String timestamp = String.valueOf(params.get(SystemParameterRenameProperties.getSysParamName(
                SystemParameterRenameProperties.TIMESTAMP)));
        Map<String, String> signMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (SystemParameterRenameProperties.DEFAULT_PARAM_MAP.containsKey(entry.getKey())) {
                continue;
            }
            if (entry.getValue() != null) {
                signMap.putAll(BeanPropertyUtil.objToStringMap(entry.getValue(), entry.getKey()));
            }
        }
        String content = EncryptUtils.sortAndMontage(signMap);
        String expected = EncryptUtils.encryptSHA1Hex(SECRET, timestamp + content + timestamp);
        return expected != null && expected.equalsIgnoreCase(String.valueOf(params.get(
                SystemParameterRenameProperties.getSysParamName(SystemParameterRenameProperties.SIGN))));
    }

    private static Map<String, Object> signedSuccessResponse(Map<String, Object> params) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", params.getOrDefault("orderId", "unknown"));
        data.put("status", "SUCCESS");

        String timestamp = String.valueOf(System.currentTimeMillis());
        String content = EncryptUtils.sortAndMontage(BeanPropertyUtil.objToStringMap(data, null));
        String sign = EncryptUtils.encryptSHA1Hex(SECRET, timestamp + content + timestamp);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("code", 0);
        wrapper.put("msg", "success");
        wrapper.put("data", data);
        wrapper.put("timestamp", Long.valueOf(timestamp));
        wrapper.put("sign", sign);
        return wrapper;
    }

    private static Map<String, Object> errorResponse() {
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("code", 1001);
        wrapper.put("msg", "业务处理失败");
        return wrapper;
    }

    private static void writeJson(HttpExchange exchange, Object body) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    static class SimpleRequest extends BaseRequest {
        private String orderId;

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }
}
