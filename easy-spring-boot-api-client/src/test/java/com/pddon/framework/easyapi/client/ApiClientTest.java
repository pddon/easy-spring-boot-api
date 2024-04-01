package com.pddon.framework.easyapi.client;

import cn.hutool.json.JSONUtil;
import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;
import com.pddon.framework.easyapi.client.consts.HttpMethod;
import com.pddon.framework.easyapi.client.request.BaseRequest;
import com.pddon.framework.easyapi.client.request.CurrencyType;
import com.pddon.framework.easyapi.client.request.PrePayRequest;
import com.pddon.framework.easyapi.client.request.TradeIdRequest;
import com.pddon.framework.easyapi.client.response.AppPreOrderResponse;
import com.pddon.framework.easyapi.client.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.client.utils.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * @ClassName: ApiClientTest
 * @Description: 接口测试
 * @Author: Allen
 * @Date: 2024-03-06 22:38
 * @Addr: https://pddon.cn
 */
@SpringBootTest(classes = {StartTestDemoConfig.class})
@RunWith(SpringRunner.class)
@Slf4j
public class ApiClientTest {
    private static ApiClient client;
    @BeforeClass
    public static void init(){
        /*client = ApiClient.newInstance(ApplicationConfig.builder()
                        .appId("webApp")
                        .baseUrl("http://localhost:8282/test/")
                        .channelId("default")
                        .locale("zh_CN")
                        .secret("NKVNcuwwEF76622A")
                        .privateSecret("")
                        .publicSecret("")
                        .build());*/
        client = ApiClient.newInstance(ApplicationConfig.builder()
                .appId("pddon-payment-demo")
                .baseUrl("http://localhost:8989/payment/")
                .channelId("local_test")
                .locale("zh_CN")
                .secret("NKVNcuwwEF3sc22A")
                .privateSecret("")
                .publicSecret("")
                .build());
    }

    @Test
    public void testGet(){
        BaseRequest request = new BaseRequest();
        DefaultResponseWrapper<Map> response = client.executeRequest("/system/errorList", HttpMethod.GET, request, Map.class, false, new HashMap<>());
        log.info(JSONUtil.toJsonStr(response));
    }

    @Test
    public void testSignBoth(){
        PrePayRequest request = PrePayRequest.builder()
                .appId(client.getConfig().getAppId())
                .orderId("easyapi_client001")
                .description("just a test!")
                .currency(CurrencyType.CNY)
                .totalAmount(1)
                .originalAmount(10)
                .userId("U008956")
                .userNickname("小明")
                .build();

        ApiInfo apiInfo = ApiInfo.builder()
                .apiName("/trade/prepay.do")
                .method(HttpMethod.POST)
                .needSign(true)
                .needSignResult(true)
                .build();
        DefaultResponseWrapper<AppPreOrderResponse> response = client.executeRequest(apiInfo, request, AppPreOrderResponse.class, new HashMap<>());
        log.info(JSONUtil.toJsonStr(response));
    }

    @Test
    public void testEncryptRequest(){
        TradeIdRequest request = TradeIdRequest.builder()
                .appId(client.getConfig().getAppId())
                .orderId("easyapi_client002")
                .build();

        ApiInfo apiInfo = ApiInfo.builder()
                .needSign(true)
                .apiName("/trade/query.do")
                .method(HttpMethod.POST)
                .needSign(true)
                .needSignResult(true)
                //.encryptRequest(true)
                //.encryptParams(new HashSet<>(Arrays.asList("orderId")))
                .build();
        DefaultResponseWrapper<Map> response = client.executeRequest(apiInfo, request, Map.class, new HashMap<>());
        log.info(JSONUtil.toJsonStr(response));
    }

    @Test
    public void testDecrypt(){
        String data = "bzosGyfredVa3avMHfuF0C+uYJbDJAm4yHwXEuMtklg=";
        try {
            log.info(EncryptUtils.encodeAES128("NKVNcuwwEF55622A", "easy_api_client001"));
            log.info(EncryptUtils.decodeAES128("NKVNcuwwEF55622A", data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
