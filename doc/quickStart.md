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
> <version>2.0.6</version>
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
> #enableLanguageCache: true
> #多语言缓存的最长时间，单位秒
> #languageCacheSeconds: 180
> #防重复提交码过期时间
> #noSubmitRepeatTimeoutSeconds: 120
> #防重复提交配置
> #no-repeat-submit:
> #  release-on-failure: true #默认true：业务失败释放防重码，允许立即重试；false为旧行为（失败也标记已提交）
> #验签配置
> #sign:
> #  replay-protection:
> #    enabled: false #默认关闭，关闭时验签行为与原来一致
> #    timestamp-window-seconds: 300 #时间戳允许的最大偏差（秒）
> #    nonce-ttl-seconds: 300 #nonce保留时间（秒）
> #    nonce-cache-size: 100000 #nonce缓存最大条目数，避免内存无界增长
> #    nonce-param: nonce #nonce参数名，需参与签名
> #    nonce-max-length: 128 #nonce最大长度
> #状态存储配置（可选Redis，默认本地；未引入spring-boot-starter-data-redis时自动回退本地）
> #state-store:
> #  type: auto #默认auto：有Redis用Redis，否则本地；local强制本地；redis强制Redis（缺失时启动失败）
> #  key-prefix: "" #默认空：easyapi:<spring.application.name或default>:
> #  fallback-to-local-on-failure: true #默认true：Redis故障回退本地并限频告警；false为fail-fast
> #  fallback-warn-interval-seconds: 60
> #用户会话失效时间
> #sessionExpireSeconds: 600
> #当未找到该语言的翻译时，是否使用默认翻译内容
> #alwaysUseDefaultLocale: true
> #是否强制对所有api响应添加响应壳
> #forceAutoAddResponseWrapper: true
> #是否禁用方法结果缓存（@CacheMethodResult）
> #disableMethodCache: false
> #是否缓存HTTP消息转换器列表，默认false（保持原行为；开启后启动期不再重复构建）
> #enableHttpMessageConverterCache: false
> #本地缓存容器配置（位于base模块，使用独立前缀）
> #local-cache:
> #  lock-free: false #默认false；true时set/remove/clear不再持有全局锁
> #异步线程池配置
> #async-thread-pool:
> #  core-pool-size: 5
> #  max-pool-size: 20
> #  queue-capacity: 10000
> #  keep-alive-seconds: 600
> #  thread-name-prefix: "EasyApi-AsyncExecutor-"
> #  reject-policy: discard #默认discard（记录日志并丢弃任务）；callerRuns为背压不丢任务
> #  fail-fast: true #默认true：core>max、负数等非法配置启动期抛异常；false时记录ERROR日志并回退默认值
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
>
> * 本仓库的`easy-spring-boot-api-demo`已加入根POM的modules，端到端测试使用随机端口启动，不占用8282端口：
>
>     `mvn -o -pl easy-spring-boot-api-demo -am test -DskipTests=false -Dtest=DemoEndToEndTest -DfailIfNoTests=false`
### java客户端代码示例

> * `pom.xml`文件引入maven依赖
>
>  ```xml
>  		<dependency>
>              <groupId>com.pddon.framework</groupId>
>              <artifactId>easy-spring-boot-api-client</artifactId>
>              <version>${easy-spring-boot-api.version}</version>
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
>          SpringApplication.run(StartClientApplication.class, args);
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

### 防重复提交失败释放与验签防重放

> **失败释放防重码**：`easyapi.no-repeat-submit.release-on-failure`默认`true`。业务成功才把提交码标记为已提交（再次请求返回`UNIQUE_TOKEN_USED`）；参数校验、验签、业务异常、系统异常等失败场景会释放提交码，客户端纠正后可立即重试。并发重复请求被`UNIQUE_TOKEN_USING`拒绝时不会释放他人持有的码。需要恢复旧行为时配置为`false`。

> **验签防重放（可选）**：`easyapi.sign.replay-protection.enabled=true`后：
> 1. 校验时间戳（毫秒）与服务器时间偏差不超过`timestamp-window-seconds`（默认300秒）；
> 2. 客户端必须通过`nonce-param`（默认`nonce`）传递nonce，服务端会把nonce加入签名参数；
> 3. 验签通过后消费nonce，重复使用返回`REPLAY_ATTACK(130008)`；nonce为空或超长返回`ERROR_SIGN(130005)`。
>
> 客户端签名内容变为：`timestamp + 排序拼接后的请求参数(含nonce) + timestamp`，可直接使用`EncryptUtils.sortAndMontage`。未开启防重放时客户端无需任何改动。nonce通过统一状态存储消费，未接入Redis时为单JVM一次校验。

### 可选Redis与分布式一致性

> 默认使用本地状态存储，行为与历史版本完全一致。引入`spring-boot-starter-data-redis`（框架中为`optional`依赖，不会传递给业务项目）并配置`spring.redis.*`后，自动启用Redis状态存储：

```yaml
easyapi:
  state-store:
    type: auto                        # 默认auto：有Redis用Redis，否则本地；local强制本地；redis强制Redis（缺失时启动失败）
    key-prefix: ""                    # 默认空：easyapi:<spring.application.name或default>:
    fallback-to-local-on-failure: true    # 默认true：Redis故障回退本地并限频告警；false为fail-fast
    fallback-warn-interval-seconds: 60
```

> 保证：防重复提交码、验签nonce跨实例唯一（`SET NX EX`）；会话、方法结果缓存、国际化翻译缓存、验证码/邮箱码随`CacheManager`一起切换到分布式实现，跨实例可读，同一key的命中与失效一致。
> 值规范：JSON信封`{"c":"原始类名","v":值}`，不使用JDK序列化；key为`<全局前缀>cache:<write|read>:<业务key>`，可直接排查。
> 仍需业务配合：同一会话并发更新为最后写入者生效（不合并）；验证码`get+remove`非原子，极并发下可能被重复校验；方法缓存无single-flight，未命中并发可能重复计算。
> 差异与风险：Redis模式没有TTL桶，`clear(expireSeconds)`会清理整个`cache:`命名空间，`stats/getAllCaches`返回空集合；默认降级策略下Redis故障期间会失去跨实例一致性（出现WARN日志），需要监控。详见README「5.5 可选Redis与分布式一致性」。
> 真实Redis集成测试默认跳过，执行命令见README 5.5.3；本机无Redis，未实测。

### 内存优化与文件上传大小

> **日志与请求体**：`ApiLogInterceptor`改为达到日志上限（请求/响应各1024字符）即停止序列化，日志字段与截断结果不变；请求参数日志在请求阶段生成一次，响应阶段复用。`RepeatedlyReadFilter`对`contentLength==0`的请求不再创建包装对象，有body时直接缓存原始字节，`getInputStream`/`getReader`仍可重复读取。
> **本地缓存容量**：`easyapi.local-cache.max-cache-size`（默认100000）、`easyapi.local-cache.max-cache-num`（默认2000）可调，默认值与改造前一致。
> **权限对象TTL**：`User:Perms`/`User:DataPerms`默认仍为注解的3600秒；需要缩短大对象驻留时间时配置
> `easyapi.method-cache.expire-seconds-by-prefix`，例如`"[User:Perms]": 600`（key必须与注解`prefix`完全一致）。
> **异步上下文**：异步任务在提交时生成请求上下文快照，attachments为不可变共享Map，任务内不能修改；需要传数据请使用方法参数。
> **方法缓存key**：默认（`easyapi.method-cache.auto-key-v2=false`）保持旧格式；设为`true`后参数值URL编码并以`&`拼接、null参数显式占位，消除`"-"`拼接与`null`省略造成的key碰撞，同时字段名改为精确匹配（`design`/`signal`不再被误当作`sign`剔除）。开启会使旧格式key全部失效、存量缓存首次回源；回滚只需改回`false`。
> **异步线程池校验**：`easyapi.async-thread-pool.fail-fast`默认`true`，非法线程池参数使启动失败；设为`false`时记录ERROR日志并回退默认值（core=5、max=20、queue=10000、keepAlive=600）。
> **文件上传（demo）**：`easy-spring-boot-api-demo`的`application.yml`/`application-prod.yml`已补充`spring.servlet.multipart`：`max-file-size: 12MB`、`max-request-size: 16MB`。不配置时Spring Boot默认1MB，1~12MB文件会在进入Controller前被拒绝。业务工程需按自己的上限同步配置。

```yaml
easyapi:
  local-cache:
    max-cache-size: 100000   # 单桶key上限，默认100000
    max-cache-num: 2000      # 桶数量上限，默认2000
  method-cache:
    expire-seconds-by-prefix:
      "[User:Perms]": 3600      # 默认不配置即沿用注解值；示例为显式写回现值
      "[User:DataPerms]": 3600
    auto-key-v2: false          # 默认false（旧格式）；true启用新key格式，旧缓存首次回源
  async-thread-pool:
    fail-fast: true             # 默认true快速失败；false回退默认值并记录ERROR日志
spring:
  servlet:
    multipart:
      max-file-size: 12MB
      max-request-size: 16MB
```

> 说明与验证结果见README「5.6 内存优化（B类）」。

### 示例工程 simple-demos

> 位置：`e:\code_repo\common-projects\easy-spring-boot-api-simple-demos`，与框架主仓库同级的独立 Maven 工程（父 POM 为 `spring-boot-starter-parent 2.3.12.RELEASE`，依赖 `easy-spring-boot-api` 2.0.8）。

工程包含 12 个可独立启动的模块，每个模块演示一个功能场景，端口分配如下：

| 模块 | 端口 | 演示内容 |
| ---- | ---- | ----- |
| validate-params-demo | 8081 | 请求/响应参数校验、系统参数校验 |
| method-result-cache-demo | 8082 | 方法结果缓存、本地缓存容器 |
| prevent-concurrent-submit-demo | 8083 | 接口防重复提交 |
| add-response-wrapper-demo | 8084 | API 响应自动包装 |
| api-sign-demo | 8085 | 接口请求/响应加签验签 |
| api-private-data-demo | 8086 | 接口数据加解密与敏感数据 |
| translate-resp-language-demo | 8087 | 接口响应国际化翻译 |
| error-code-demo | 8088 | 业务错误码与系统错误 |
| api-doc-demo | 8089 | Swagger/Knife4j 接口文档 |
| api-log-interceptor-demo | 8090 | API 日志、traceId 与拦截器扩展 |
| async-thread-pool-demo | 8091 | 异步线程池与 reject-policy |
| java-client-demo | 8092 | Java 客户端 SDK 调用 |

* 构建（跳过测试）：`mvn -o -DskipTests package`
* 运行全部测试：`mvn -o test`（共 80 个用例，测试均以 `--server.port=0` 随机端口启动，不占用上表中的固定端口）
* 启动单个模块：`mvn -o -pl <module> spring-boot:run`，或在 IDE 中直接运行该模块的 `Startup` 启动类

### 总结

以上就是easy-spring-boot-api框架的服务器、java客户端、typescript客户端代码示例，有没有感觉特别容易上手，本文仅做抛砖引玉，其他更多好用功能还等大家一起来体验，喜欢的小伙伴不要忘了点star哦！


