> 兄弟们，真不骗你们，这个框架用起来是真的爽，简直是服务器开发人员的福音！
>
> * 集成该项目后，不用我们程序员再去处理api安全、加签、验签、参数校验、加解密、数据脱敏、异常处理、国际化、接口文档、错误码、缓存、分布式锁、应用、渠道管理等等功能。
> * 而且为了帮助客户端开发的同学更简单的接入后端api，它还提供了java版和Typescript版本的客户端工具包，而且也都是开源的。
> * 不仅如此，而且像验签算法、加解密算法、请求参数、响应参数、错误码等等功能还支持灵活定制化。
> * 在尝试使用了一下，发现这个框架使用起来非常方便，java版服务端和客户端只需要引入一个注解即可启用所有功能

### 开源项目地址：
* `EasyApi` java版服务端和客户端源码：[https://github.com/pddon/easy-spring-boot-api](https://github.com/pddon/easy-spring-boot-api)
* `EasyApi` typescript版客户端源码: [https://github.com/pddon/easy-api-client](https://github.com/pddon/easy-api-client)

> 目测该项目刚开源没几天，如果觉得不错的老铁，可以动动你发财的手指，给它点点star哦！

### 使用示例

#### 服务端代码

我们采用它们官方的示例代码编写服务器端代码
> * `pom.xml`文件引入maven依赖
> ```xml
> <!-- 基于springboot的api框架 -->
> <dependency>
> <groupId>com.pddon.framework</groupId>
> <artifactId>easy-spring-boot-api</artifactId>
> <version>2.0.1</version>
> </dependency>
> ```
>
> * 添加`springboot`启动类`StartServerApplication.java`
>
> ```java
> @SpringBootApplication
> @EnableEasyApi
> public class StartServerApplication {
> 
> 	public static void main(String[] args) {
> 		SpringApplication.run(StartServerApplication.class, args);
> 	}
> }
> ```
>
> * `application.yml`添加配置
>
> ```yaml
> server:
> port: 8282
> servlet:
>   context-path: /test
> easyapi:
> #全局默认配置
> #是否启用EasyApi框架
> #enable: true
> #是否开启自动生成swagger接口文档
> #enableSwagger: true
> #默认语言,请求中或者会话中不存在语言参数时的默认取值
> #locale: "zh_CN"
> #是否开启多语言翻译缓存
> #enableLanguageCache: false
> #多语言缓存的最长时间，单位秒
> #languageCacheSeconds: 180
> #防重复提交码过期时间
> #noSubmitRepeatTimeoutSeconds: 120
> #用户会话失效时间
> #sessionExpireSeconds: 600
> #当未找到该语言的翻译时，是否使用默认翻译内容
> #alwaysUseDefaultLocale: false
> #是否强制对所有api响应添加响应壳
> #forceAutoAddResponseWrapper: true
> #根包名集合,以逗号分隔
> basePackages: "com.pddon.framework.demo.easyapi"
> #是否在控制台打印所有配置信息
> #printAllProperties: true
> error: #错误码相关配置
>   #自定义系统错误码值,格式【枚举名 ：数值】
>   systemErrorCodes:
>     NOT_FOUND: 404
>   #业务异常定义，格式【错误码多语言字典值 ：错误码指代的默认中文翻译描述信息】
>   #业务错误码默认起始值
>   businessCodeStart: 200001
>   #业务错误码默认最大值
>   businessCodeEnd: 999999
>   #业务异常错误码值
>   businessErrorCodes:
>     USER_ACCOUNT_NOT_FOUND.desc: "用户账号[{0}]未找到!"
>     ACCOUNT_NOT_FOUND:
>       code: 200006
>       desc: "账户[{0}]未找到！"
> api:
>   #业务接口api包名
>   swagger:
>     #业务接口所在根包名，swagger会扫描这个包下的controller，生成您项目中中api的接口文档
>     basePackage: "com.xxx.xxx"
>     title: "业务API"
>     description: "所有业务接口列表"
>     termsOfServiceUrl: "www.xxx.com"
>     contact: "xxx@xxx.xxx"
>     version: "v1.0.0"
>   #api请求系统参数配置
>   request:
>     parameter:
>       otherParams: #其他额外系统参数
>               - "appName"
>               - "nickName"
>               - "tagId"
>         #rename: #重命名系统参数
>         #channelId: "buId"
>         #api响应系统参数配置
>         #response:
>         #field:
>         #rename: #重命名系统响应参数
>         #code: "status"
>   #渠道、应用信息配置
>   channels:
>     default: #用于不区分渠道信息的应用
>       default: #默认的秘钥信息
>         enable: true
>         #用于对称加解密、生成数字签名、验证数字签名的秘钥
>         secret: "EEFGcuwwEF76622A"
>         keyPair: #用于非对称加解密的秘钥对
>           privateSecret: ""
>           publicSecret: ""
>       webApp: #某个应用下的秘钥信息
>         enable: true
>         #用于对称加解密、生成数字签名、验证数字签名的秘钥
>         secret: "NKVNcuwwEF76622A"
>         keyPair: #用于非对称加解密的秘钥对
>           privateSecret: ""
>           publicSecret: ""
>     android:
>       default:
>         enable: true
>         #用于对称加解密、生成数字签名、验证数字签名的秘钥
>         secret: "NK445uwwEF76622A"
>         keyPair: #用于非对称加解密的秘钥对
>           privateSecret: ""
>           publicSecret: ""
>     ios:
>       default:
>         enable: true
>         secret: "NKVNcuww89A6622A"
>         keyPair:
>           privateSecret: ""
>           publicSecret: ""
> 
> logging:
> level:
>   org.springframework.web: INFO
>   com.pddon.framework.easyapi: TRACE
> ```
>  * 编写测试api，`TestController.java`
>
>  ```java
>  	 @RestController
>      public class TestController {
>          
>      	@PostMapping("prepay.do")
>          @ApiOperation(value="应用下单", notes="应用向支付中心预下单")
>          @RequiredSign(scope = SignScope.BOTH)
>          public AppPreOrderResponse prepay(@RequestBody PrePayRequest request){
>              return service.prepay(request);
>          }
>      }
>  ```
> * 运行成功后，打开接口文档地址
>
>     `http://localhost:8282/test/doc.html`
>
>     也就是swagger-ui的国内升级版：knife4j，比swagger-ui的界面要好看多了。
### java客户端代码示例

> * `pom.xml`文件引入maven依赖
>
>  ```xml
>  		<dependency>
>              <groupId>com.pddon.framework</groupId>
>              <artifactId>easy-spring-boot-api-client</artifactId>
>              <version>1.0.0</version>
>          </dependency>
>  ```
>
> * 添加`springboot`启动类`StartClientApplication.java`
>
>  ```java
>  @SpringBootApplication
>  @EnableEasyApiClient
>  public class StartClientApplication {
>      public static void main(String[] args) {
>          SpringApplication.run(StartServer.class, args);
>      }
>  }
>  ```
>
> * `application.yml`添加配置
>
>  ```yaml
>  easyapi:
>    client:
>      app:
>        baseUrl: http://localhost:8282/test/
>        channelId: default      
>        appId: webApp
>        secret: NKVNcuwwEF76622A
>        locale: zh_CN
>        privateSecret:
>        publicSecret:
>  ```
>
> * 接口调用使用示例`ClientApiTest.java`
>
>  ```java
>  @Component
>  public class ClientApiTest {
>  
>      @Autowired
>      private ApplicationConfig applicationConfig;
>  
>      private static ApiClient client;
>      
>      @PostConstruct
>      public void init(){
>          client = ApiClient.newInstance(applicationConfig);
>      }
>  
>      public DefaultResponseWrapper<AppPreOrderResponse> prepay(){
>          PrePayRequest request = PrePayRequest.builder()
>                  .appId(client.getConfig().getAppId())
>                  .orderId("easyapi_client001")
>                  .description("just a test!")
>                  .currency(CurrencyType.CNY)
>                  .totalAmount(1)
>                  .originalAmount(10)
>                  .userId("U008956")
>                  .userNickname("小明")
>                  .build();
>  
>          ApiInfo apiInfo = ApiInfo.builder()
>                  .apiName("/trade/prepay.do")
>                  .method(HttpMethod.POST)
>                  .needSign(true)
>                  .needSignResult(true)
>                  .build();        
>          DefaultResponseWrapper<AppPreOrderResponse> response = client.executeRequest(apiInfo, request, AppPreOrderResponse.class, new HashMap<>());
>          log.info(JSONUtil.toJsonStr(response));
>          return response;
>      }
>  }
>  ```
> * 结果
>
>     ```txt
>     2024-03-14 20:14:24.284  INFO 4820 --- [           main] c.p.f.easyapi.client.ClientApiTest       : {"code":0,"data":{"outTradeNo":"202403142011433578389649","orderId":"easyapi_client001","redirectUrl":"https://www.xxx.com/h5.html"},"sign":"A3731FA3BA2704BC51F4633DCC2C1BD4B232A41B","timestamp":1710418464218}
>     ```



### Typescript客户端调用示例

> * 安装依赖
>
>  ```shell
>  npm i easy-api-client
>  ```
>
>  或者：
>
>  ```shell
>  yarn add easy-api-client
>  ```
>
> * 编写测试代码
>
>  ```typescript
>  import {ApiClient, newApiClientInstance, RequestType} from "easy-api-client";
>  let client: ApiClient = newApiClientInstance({
>      baseUrl: "http://localhost:8989/payment/",
>      channelId: "local_test",
>      appId: "pddon-payment-demo",
>      secret: "NKVNcuwwEF3sc22A",
>      locale: "zh_CN",
>      apiSuffix: ".do"
>  });
>  client.request({
>      apiName: "/trade/prepay",
>      type: RequestType.post,
>      needSignResult: true,
>      needSign: true
>  }, {
>      appId: client.options.appId,
>      orderId: "easy_api_client001",
>      description: "just a test!",
>      currency: "CNY",
>      totalAmount: 1,
>      originalAmount: 5,
>      userId: "U008958",
>      userNickname: "小明"
>  }).then((response: any) => {
>      console.log(response.data);
>  }, (reason: any) => {
>      console.log(reason);
>  });
>  ```
>
> * 调用结果
>
>  ```txt
>  {
>    code: 0,
>    data: {
>      outTradeNo: '202403112208576967291582',
>      orderId: 'easy_api_client001',
>      redirectUrl: 'https://www.xxx.com/h5.html'
>    },
>    sign: '3DB9F78499E8F31D3800FA3415009BE3D19EBA19',
>    timestamp: 1710418049919
>  }
>  ```
>
>  

### 总结

以上就是easy-spring-boot-api框架的服务器、java客户端、typescript客户端代码示例，有没有感觉特别容易上手，本文仅做抛砖引玉，其他更多好用功能还等大家一起来体验，喜欢的小伙伴不要忘了点star哦！


