package com.pddon.framework.easyapi;

import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.pddon.framework.demo.easyapi.controller.dto.SnakeNode;
import com.pddon.framework.demo.easyapi.startup.Startup;
import com.pddon.framework.easyapi.client.ApiClient;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;
import com.pddon.framework.easyapi.client.consts.HttpMethod;
import com.pddon.framework.easyapi.controller.request.BaseRequest;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;

import java.util.HashMap;

/**
 * demo端到端测试：随机端口启动demo服务，再用客户端SDK发起真实HTTP请求，
 * 覆盖服务端拦截器链、响应包装与响应验签，以及参数自动校验异常链路。
 * 避免依赖固定端口8282与外部环境。
 */
public class DemoEndToEndTest {

    private static ConfigurableApplicationContext serverContext;
    private static ConfigurableApplicationContext clientContext;
    private static ApiClient apiClient;
    private static int port;

    @BeforeClass
    public static void setUp() {
        serverContext = new SpringApplicationBuilder(Startup.class)
                .web(WebApplicationType.SERVLET)
                .run("--server.port=0", "--server.servlet.context-path=/test");
        port = ((ServletWebServerApplicationContext) serverContext).getWebServer().getPort();

        clientContext = new SpringApplicationBuilder(ClientTestApplication.class)
                .web(WebApplicationType.NONE)
                .properties(
                        "easyapi.client.app.baseUrl=http://127.0.0.1:" + port + "/test/",
                        "easyapi.client.app.appId=webApp",
                        "easyapi.client.app.channelId=default",
                        "easyapi.client.app.locale=zh_CN",
                        "easyapi.client.app.secret=NKVNcuwwEF76622A")
                .run();
        apiClient = clientContext.getBean(ApiClient.class);
    }

    @AfterClass
    public static void tearDown() {
        if (clientContext != null) {
            clientContext.close();
        }
        if (serverContext != null) {
            serverContext.close();
        }
    }

    /**
     * 客户端测试上下文：开启自动配置以提供ObjectMapper，并扫描客户端SDK组件
     */
    @Configuration
    @EnableAutoConfiguration
    @ComponentScan("com.pddon.framework.easyapi.client")
    static class ClientTestApplication {
    }

    @Test
    public void testResponseSignWithCyclicObject() {
        DefaultResponseWrapper<SnakeNode> response = apiClient.executeRequest(
                ApiInfo.builder()
                        .apiName("testCircleDependentSign")
                        .method(HttpMethod.GET)
                        .needSign(false)
                        .needSignResult(true)
                        .build(),
                new BaseRequest(), SnakeNode.class, new HashMap<>());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.isSuccess());
        SnakeNode node = response.getData();
        Assert.assertNotNull(node);
        Assert.assertEquals("red", node.getColor());
        Assert.assertNotNull(node.getNext());
        Assert.assertEquals("blue", node.getNext().getColor());
        Assert.assertEquals("black", node.getNext().getNext().getColor());
    }

    @Test
    public void testResponseWrapperAndListSerialization() {
        DefaultResponseWrapper<List> response = apiClient.executeRequest(
                ApiInfo.builder()
                        .apiName("testEncryptListResponseData")
                        .method(HttpMethod.GET)
                        .needSign(false)
                        .build(),
                new BaseRequest(), List.class, new HashMap<>());

        Assert.assertNotNull(response);
        Assert.assertTrue(response.isSuccess());
        Assert.assertNotNull(response.getData());
        Assert.assertEquals(2, response.getData().size());
        Map first = (Map) response.getData().get(0);
        Assert.assertEquals("danyuan", first.get("username"));
    }

    /**
     * 响应参数非法时，ValidateParamsInterceptor在afterInvoke中抛出BindException，
     * 经全局异常处理后返回业务错误码，客户端能正常解析。
     */
    @Test
    public void testResponseValidationFailureThroughInterceptorChain() {
        DefaultResponseWrapper<Map> response = apiClient.executeRequest(
                ApiInfo.builder()
                        .apiName("testResponseFieldCheck")
                        .method(HttpMethod.GET)
                        .needSign(false)
                        .build(),
                new BaseRequest(), Map.class, new HashMap<>());

        Assert.assertNotNull(response);
        Assert.assertFalse("响应校验失败应返回非成功错误码", response.isSuccess());
    }
}
