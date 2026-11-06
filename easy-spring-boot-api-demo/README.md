<center><h1>easy-spring-boot-api框架特性介绍完整示例</h1></center>

#### 1、简介

> 此项目用于演示easyapi提供的大部分功能，着重介绍特性，不做深入细节演示，如果需要更深入的理解和使用，请参考[更多示例](../doc/demos.md)。
>
> **本地运行与测试（本次优化补充）**：
>
> * `easy-spring-boot-api-demo`已加入根POM的modules，默认配置`server.port=8282`、`server.servlet.context-path=/test`；
> * 客户端SDK示例配置与`src/main/resources/application.yml`一致：`appId=webApp`、`channelId=default`、`secret=NKVNcuwwEF76622A`；
> * 端到端测试`DemoEndToEndTest`通过`--server.port=0`随机端口启动，并用客户端SDK发起真实HTTP请求，不会与本地8282端口冲突；
> * 运行测试：`mvn -o -pl easy-spring-boot-api-demo -am test -DskipTests=false -Dtest=DemoEndToEndTest -DfailIfNoTests=false`。
> * 文件上传：`application.yml`与`application-prod.yml`已配置`spring.servlet.multipart.max-file-size=12MB`、`max-request-size=16MB`，与`FileController`的12MB校验一致；缺少该配置时Spring Boot默认1MB，1~12MB文件会在进入Controller前被拒绝。业务工程请按自己的上限同步修改。

#### 2、构建与运行

> 构建命令：`mvn -o -pl easy-spring-boot-api-demo -am package -DskipTests`
>
> 产物说明（`target/`下）：
>
> * `easy-spring-boot-api-demo-<version>.jar`：普通瘦jar，仅含demo自身类，**不可直接启动**，供依赖或再发布使用；
> * `easy-spring-boot-api-demo-<version>-exec.jar`：Spring Boot标准可执行jar，依赖在`BOOT-INF/lib`，`Main-Class=org.springframework.boot.loader.JarLauncher`、`Start-Class=Startup`，**唯一可直接运行的产物**；
> * `easy-spring-boot-api-demo-<version>-bin.zip`：仅在`mvn install`时由assembly生成，用于`lib/ + conf/ + bin/`目录布局的脚本化部署，不是可执行jar。
>
> 运行（示例使用随机端口，避免与本机已运行服务冲突）：
>
> ```bash
> java -jar target/easy-spring-boot-api-demo-<version>-exec.jar --server.port=0
> ```
>
> 启动后日志会打印实际端口（例如`Undertow started on port(s) <随机端口> (http) with context path '/test'`），可用实际端口验证：
>
> ```bash
> curl "http://127.0.0.1:<实际端口>/test/testMethodQueryCache?appId=webApp&channelId=default&timestamp=<毫秒时间戳>"
> ```
>
> 返回`{"code":0,"data":"this is testMethodQueryCache result !"}`即表示可执行jar运行正常。
>
> 不再提供`jar-with-dependencies`胖jar：该形式会覆盖Spring Boot自动配置的`META-INF/spring.factories`，且`Main-Class`固定为不存在的`Main`，无法启动。
>
> **部署包`-bin.zip`（仅在`mvn install`阶段生成）**：
>
> * 目录布局：`bin/`（`startup.sh`、`shutdown.sh`、`serverStatus.sh`）、`conf/`（外置`application.yml`）、`lib/`（全部运行时依赖，已排除`byte-buddy`等测试期依赖）；
> * **启动脚本仅适用于POSIX环境**（Linux/macOS），需要`sh`/`bash`、`JAVA_HOME`、`jps`；**Windows下请直接使用`-exec.jar`**，部署包不提供`.bat`/`.cmd`；
> * 外置配置优先级：`conf/application.yml` > jar内`classpath:/application.yml`，**只覆盖同名键**，jar内其余配置（`easyapi.basePackages`、`easyapi.api.channels`等）继续生效。`startup.sh`仅在`conf/application.yml`（或`.properties`）存在时追加`--spring.config.additional-location=file:$APP_HOME/conf/`；不把`conf`放进`-classpath`，因为Boot对`classpath:/application.yml`只取第一个命中的资源，`conf`一旦进入classpath会整体遮蔽jar内同名文件；
> * `conf/application.yml`可覆盖项（均为易变、非密钥项）：`server.port`、`server.undertow.threads.io`/`worker`、`server.undertow.buffer-size`、`direct-buffers`、`max-http-post-size`、`logging.level.*`、`easyapi.state-store.type`/`key-prefix`/`fallback-to-local-on-failure`/`fallback-warn-interval-seconds`、`spring.redis.*`（默认以注释形式占位，填真实地址/密码后不要提交到仓库）。业务密钥（`channels.*.secret`）不要放在部署包配置中。
> * 已知风险：`lib/`中`swagger-annotations`（`1.5.22`与`2.1.2`）、`swagger-models`（`1.5.22`与`2.1.2`）各存在双版本（框架显式依赖与knife4j传递依赖冲突），扁平classpath下生效顺序取决于文件名顺序；`-bin.zip`与`-exec.jar`两种方式均实测可正常启动，本次未调整pom。
