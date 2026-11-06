# java客户端调用API使用方法

> easy-spring-boot-api-client客户端API调用包用于快速接入服务端api框架easy-spring-boot-api，支持同一服务中实例化多个client，也支持服务端api和client同时在一个服务内使用。
> 
> **说明（本次优化核对）**：下文第3节的`/trade/prepay.do`示例来自配套的支付demo项目；若要对本仓库的demo做端到端联调，请使用`easy-spring-boot-api-demo`中的`testCircleDependentSign`（GET，响应验签）或`testEncryptListResponseData`（GET），可直接参考`easy-spring-boot-api-demo/src/test/java/com/pddon/framework/easyapi/DemoEndToEndTest.java`。本仓库demo的端到端测试中客户端配置为`baseUrl=http://127.0.0.1:<随机端口>/test/`、`appId=webApp`、`channelId=default`、`secret=NKVNcuwwEF76622A`。
> 

## 1. 安装依赖

```xml
    <dependency>
        <groupId>com.pddon.framework</groupId>
        <artifactId>easy-spring-boot-api-client</artifactId>
        <version>${easy-spring-boot-api.version}</version>
    </dependency>
```

## 2. 配置EasyApi服务端地址等信息

```yaml
easyapi:
  client:
    app:
      baseUrl: http://localhost:8282/test/
      channelId: default      
      appId: webApp
      secret: NKVNcuwwEF76622A
      locale: zh_CN
      privateSecret: xxx
      publicSecret: xxx
```

## 3. 接口调用使用示例

* 添加`springboot`启动类`StartClientApplication.java`

  ```java
  @SpringBootApplication
  @EnableEasyApiClient
  public class StartClientApplication {
      public static void main(String[] args) {
          SpringApplication.run(StartClientApplication.class, args);
      }
  }
  ```

* 接口调用示例类`ClientApiTest.java`

  ```java
  @Component
  public class ClientApiTest {

    @Autowired
    private ApiClient client;
    
    public DefaultResponseWrapper<AppPreOrderResponse> prepay(){
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
          return response;
    }
  }
  ```
* 接口调用结果示例
  ```text
    2024-03-14 20:14:24.284  INFO 4820 --- [           main] c.p.f.easyapi.client.ClientApiTest       : {"code":0,"data":{"outTradeNo":"202403142011433578389649","orderId":"easyapi_client001","redirectUrl":"https://www.xxx.com/h5.html"},"sign":"A3731FA3BA2704BC51F4633DCC2C1BD4B232A41B","timestamp":1710418464218}
  ```