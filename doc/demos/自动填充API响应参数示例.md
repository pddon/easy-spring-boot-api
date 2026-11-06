<center><h1>easy-spring-boot-api框架自动填充API响应参数示例</h1></center>

# 简介
> 此项目用于演示如何easyapi如何提供统一标准的JSON格式的API响应信息，把开发者从繁琐的API响应处理工作中解脱出来。

* [demo源码传送门](../../../easy-spring-boot-api-simple-demos/add-response-wrapper-demo)

# 项目示例

## 1 项目结构

* 项目结构

```ruby
│  pom.xml
│
└─src
    ├─main
    │  ├─java
    │  │  └─com
    │  │      └─pddon
    │  │          └─framework
    │  │              └─demo
    │  │                  └─easyapi
    │  │                      ├─startup
    │  │                      │      Startup.java #项目启动类
    │  │                      │
    │  │                      └─wrapper
    │  │                              ResponseWrapperController.java
    │  │                              UserDto.java
    │  └─resources
    │          application.yml #服务端口8084
    └─test
        └─java
            └─com
                └─pddon
                    └─framework
                        └─demo
                            └─easyapi
                                └─wrapper
                                        ResponseWrapperEndToEndTest.java
```

* 引入模块依赖，在`pom.xml`添加

```xml
<dependency>
    <groupId>com.pddon.framework</groupId>
    <artifactId>easy-spring-boot-api</artifactId>
</dependency>
```

> 版本由父工程`easy-spring-boot-api-simple-demos`的`dependencyManagement`统一管理（`easy-spring-boot-api.version` = 2.0.8）。

## 2 启用EasyApi框架

```java
package com.pddon.framework.demo.easyapi.startup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.pddon.framework.easyapi.annotation.EnableEasyApi;

/**
 * 响应统一包装示例启动类。
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.pddon.framework.demo.easyapi")
@EnableEasyApi
public class Startup {

    public static void main(String[] args) {
        SpringApplication.run(Startup.class, args);
    }
}
```

## 3 API响应自动包装示例

### 3.1 返回原始值

> 我们在开发接口时，在某些特定场景下，需要返回接口原始的返回值，有以下几种方式可以实现返回原始类型的值，你可以根据不同的应用场景进行选择
>
> | 方法                     | 类型                                           | 说明                                     | 范围           |
> | ------------------------ | ---------------------------------------------- | ---------------------------------------- | -------------- |
> | 直接返回包装好的响应信息 | `DefaultResponseWrapper`                       | 针对特定的接口使用                       | 使用的接口生效 |
> | 使用注解修饰             | `@IgnoreResponseWrapper`                       | 使某个接口直接返回原始值                 | 使用的接口生效 |
> | 关闭自动包装响应体功能   | easyapi.forceAutoAddResponseWrapper = false | 关闭easyapi提供的响应包装功能         | 全局生效       |
> | 关闭easyapi框架       | easyapi.enable = false                      | 关闭easyapi框架，失去其提供的所有特性 | 全局生效       |

> 差异说明：原文档示例的`TestOriginalResponseController`（`original/emptyResponse`、`intResponse`、`booleanResponse`、`mapResponse`、`listResponse`、`customObjectResponse`、`wrappedResponse`）在本模块中不存在，实际接口为`ResponseWrapperController`（包`com.pddon.framework.demo.easyapi.wrapper`，路径前缀`/wrapper`），对应的原始值接口为`GET /wrapper/wrapped`和`GET /wrapper/ignore`；DTO由`CustomResponse`替换为`UserDto`（字段`userName`、`age`）。

* 接口源码`ResponseWrapperController.java`

```java
package com.pddon.framework.demo.easyapi.wrapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pddon.framework.easyapi.annotation.IgnoreResponseWrapper;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;

/**
 * 响应统一包装示例。
 */
@RestController
@RequestMapping("/wrapper")
public class ResponseWrapperController {

    /**
     * 普通对象会被自动包装为 {code,data,...}。
     */
    @GetMapping("/user")
    public UserDto user() {
        return new UserDto("张三", 18);
    }

    /**
     * 已经是标准响应壳时不会重复包装。
     */
    @GetMapping("/wrapped")
    public DefaultResponseWrapper<UserDto> wrapped() {
        return new DefaultResponseWrapper<>(new UserDto("李四", 20));
    }

    /**
     * 字符串响应同样会被包装为 {code,data}。
     */
    @GetMapping("/string")
    public String string() {
        return "hello";
    }

    /**
     * 标注 @IgnoreResponseWrapper 后返回原始内容，不再包装。
     */
    @GetMapping("/ignore")
    @IgnoreResponseWrapper
    public UserDto ignore() {
        return new UserDto("王五", 22);
    }
}
```

* 返回对象`UserDto.java`

```java
package com.pddon.framework.demo.easyapi.wrapper;

/**
 * 普通业务响应对象。
 */
public class UserDto {

    private String userName;
    private Integer age;

    public UserDto() {
    }

    public UserDto(String userName, Integer age) {
        this.userName = userName;
        this.age = age;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
```

* 请求示例

```shell
curl "http://localhost:8084/wrapper/wrapped?appId=webApp&channelId=default"
curl "http://localhost:8084/wrapper/ignore?appId=webApp&channelId=default"
```

### 3.2 自动包装响应体

> 只要不关闭自动包装响应信息，easyapi将自动检测API返回的响应内容是否为规范的响应格式，会自动进行包装响应壳。

* 接口源码（`ResponseWrapperController.java`节选）

```java
    /**
     * 普通对象会被自动包装为 {code,data,...}。
     */
    @GetMapping("/user")
    public UserDto user() {
        return new UserDto("张三", 18);
    }

    /**
     * 字符串响应同样会被包装为 {code,data}。
     */
    @GetMapping("/string")
    public String string() {
        return "hello";
    }
```

> 差异说明：原文档示例的`TestAutoWrapResponseController`（`wrapped/emptyResponse`、`intResponse`、`booleanResponse`、`mapResponse`、`listResponse`、`customObjectResponse`、`wrappedResponse`）在本模块中不存在，本模块只提供`GET /wrapper/user`和`GET /wrapper/string`两个自动包装示例接口。

* 请求示例

```shell
curl "http://localhost:8084/wrapper/user?appId=webApp&channelId=default"
curl "http://localhost:8084/wrapper/string?appId=webApp&channelId=default"
```

## 4 配置信息

> 我们还可以全局配置是否开启easyapi框架和默认是否自动对原始返回值进行包装。本示例的`application.yml`：

```yaml
server:
  port: 8084
spring:
  application:
    name: add-response-wrapper-demo
easyapi:
  basePackages: com.pddon.framework.demo.easyapi
  #是否强制对所有api响应添加响应壳
  forceAutoAddResponseWrapper: true
  api:
    channels:
      default:
        webApp:
          secret: NKVNcuwwEF76622A
```
