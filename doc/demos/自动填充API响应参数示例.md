<center><h1>easy-spring-boot-api框架自动填充API响应参数示例</h1></center>

# 简介
> 此项目用于演示如何easyapi如何提供统一标准的JSON格式的API响应信息，把开发者从繁琐的API响应处理工作中解脱出来。

* [demo源码传送门](../../easy-spring-boot-api-simple-demos/add-response-wrapper-demo)

# 项目示例

## 1 项目结构

* 项目结构

    ```ruby
    │  pom.xml
    │  README.md
    │      
    └─src
        ├─main
        │  ├─java
        │  │  └─com
        │  │      └─pddon
        │  │          └─framework
        │  │              └─demo
        │  │                  └─easyapi
        │  │                      │  StartDemoApplication.java
        │  │                      │  
        │  │                      └─controller
        │  │                          │  TestAutoWrapResponseController.java
        │  │                          │  TestOriginalResponseController.java
        │  │                          │  
        │  │                          └─dto
        │  │                                  CustomResponse.java
        │  │                                  
        │  └─resources
        │          application.yml
        │          
        └─test
            └─java
                └─com
                    └─pddon
                        └─framework
                            └─easyapi
                                └─mvc
    ```

* 引入模块依赖，在`pom.xml`添加

```xml
	<dependency>
		<groupId>com.pddon.framework</groupId>
		<artifactId>easy-spring-boot-api</artifactId>
		<version>${easy-spring-boot-api.version}</version>
	</dependency>
```

## 2 启用EasyApi框架

```java
/**  
* Title StartDemoApplication.java  
* Description  
* @author danyuan
* @date Oct 31, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.pddon.framework.easyapi.annotation.EnableEasyApi;

@SpringBootApplication
@EnableEasyApi
public class StartDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartDemoApplication.class, args);
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

* 接口源码`TestOriginalResponseController.java`

    ```java
    /**  
     * Title TestOriginalResponseController.java  
     * Description  返回原始的响应值，有如下几种场景
     * 1、如果返回的响应已经附带响应壳，easyapi将不会重复包装响应内容
     * 2、如果响应内容被@IgnoreResponseWrapper注解修饰，将直接返回原始内容
     * 3、关闭自动包装响应体功能，配置属性easyapi.forceAutoAddResponseWrapper = false 也可以关闭自动包装响应信息
     * 4、关闭easyapi框架，配置属性easyapi.enable = false，所有easyapi提供的能力均失效，自然也可以返回原始内容
     * 
     * @author danyuan
     * @date Jan 3, 2021
     * @version 1.0.0
     * site: pddon.cn
     */
    package com.pddon.framework.demo.easyapi.controller;
    
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    
    import io.swagger.annotations.Api;
    import io.swagger.annotations.ApiOperation;
    
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;
    
    import com.pddon.framework.demo.easyapi.controller.dto.CustomResponse;
    import com.pddon.framework.easyapi.annotation.IgnoreResponseWrapper;
    import com.pddon.framework.easyapi.response.controller.DefaultResponseWrapper;
    
    @Api(tags = "返回原始的响应值接口列表")
    @RestController
    @RequestMapping("original")
    public class TestOriginalResponseController {
    
    	@GetMapping("emptyResponse")
    	@ApiOperation(value = "测试返回空原始值", notes = "测试返回空原始值")
    	@IgnoreResponseWrapper
    	public void emptyResponse() {
    
    	}
    
    	@GetMapping("intResponse")
    	@ApiOperation(value = "测试返回整型原始值", notes = "测试返回整型原始值")
    	@IgnoreResponseWrapper
    	public int intResponse() {
    		return 22;
    	}
    
    	@GetMapping("booleanResponse")
    	@ApiOperation(value = "测试返回boolean型原始值", notes = "测试返回boolean型原始值")
    	@IgnoreResponseWrapper
    	public boolean booleanResponse() {
    		return true;
    	}
    
    	@GetMapping("stringResponse")
    	@ApiOperation(value = "测试返回字符串型原始值", notes = "测试返回字符串型原始值")
    	@IgnoreResponseWrapper
    	public String stringResponse() {
    		return "this just a string response test !";
    	}
    
    	@GetMapping("mapResponse")
    	@ApiOperation(value = "测试返回map型原始值", notes = "测试返回map型原始值")
    	@IgnoreResponseWrapper
    	public Map<String, Object> mapResponse() {
    		// mock数据
    		Map<String, Object> userInfos = new HashMap<>();
    		userInfos.put("username", "小明");
    		userInfos.put("age", 23);
    		userInfos.put("sex", "男");
    		userInfos.put("desc", "阳光乐观外向，喜欢唱歌、打篮球！");
    
    		// 返回数据
    		return userInfos;
    	}
    	
    	@GetMapping("listResponse")
    	@ApiOperation(value = "测试返回list型原始值", notes = "测试返回list型原始值")
    	@IgnoreResponseWrapper
    	public List<String> listResponse() {
    		// mock数据
    		List<String> list = new ArrayList<>();
    		list.add("first");
    		list.add("second");
    		list.add("third");
    		// 返回数据
    		return list;
    	}
    	
    	@GetMapping("customObjectResponse")
    	@ApiOperation(value = "测试返回自定义对象类型原始值", notes = "测试返回自定义对象类型原始值")
    	@IgnoreResponseWrapper
    	public CustomResponse customObjectResponse() {
    		// mock数据
    		CustomResponse resp = new CustomResponse();
    		resp.setUsername("小明")
    			.setAge(23)
    			.setSex(true)
    			.setDesc("阳光、帅气、活泼、积极、向上！");
    		// 返回数据
    		return resp;
    	}
    	
    	@GetMapping("wrappedResponse")
    	@ApiOperation(value = "测试返回已经被包装过的原始值", notes = "测试返回已经被包装过的原始值")
    	public DefaultResponseWrapper<List<String>> wrappedResponse() {
    		// mock数据
    		List<String> list = new ArrayList<>();
    		list.add("first");
    		list.add("second");
    		list.add("third");
    		// 返回数据
    		return new DefaultResponseWrapper<>(list);
    	}
    }
    ```

### 3.2 自动包装响应体

> 只要不关闭自动包装响应信息，easyapi将自动检测API返回的响应内容是否为规范的响应格式，会自动进行包装响应壳。

* 接口源码`TestAutoWrapResponseController.java`

    ```java
    /**  
     * Title TestAutoWrapResponseController.java  
     * Description  返回包装后的响应值
     * 
     * @author danyuan
     * @date Jan 3, 2021
     * @version 1.0.0
     * site: pddon.cn
     */
    package com.pddon.framework.demo.easyapi.controller;
    
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    
    import io.swagger.annotations.Api;
    import io.swagger.annotations.ApiOperation;
    
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;
    
    import com.pddon.framework.demo.easyapi.controller.dto.CustomResponse;
    import com.pddon.framework.easyapi.response.controller.DefaultResponseWrapper;
    
    @Api(tags = "返回自动包装后的响应值接口列表")
    @RestController
    @RequestMapping("wrapped")
    public class TestAutoWrapResponseController {
    
    	@GetMapping("emptyResponse")
    	@ApiOperation(value = "测试返回空原始值", notes = "测试返回空原始值")
    	public void emptyResponse() {
    
    	}
    
    	@GetMapping("intResponse")
    	@ApiOperation(value = "测试返回整型原始值", notes = "测试返回整型原始值")
    	public int intResponse() {
    		return 22;
    	}
    
    	@GetMapping("booleanResponse")
    	@ApiOperation(value = "测试返回boolean型原始值", notes = "测试返回boolean型原始值")
    	public boolean booleanResponse() {
    		return true;
    	}
    
    	@GetMapping("stringResponse")
    	@ApiOperation(value = "测试返回字符串型原始值", notes = "测试返回字符串型原始值")
    	public String stringResponse() {
    		return "this just a string response test !";
    	}
    
    	@GetMapping("mapResponse")
    	@ApiOperation(value = "测试返回map型原始值", notes = "测试返回map型原始值")
    	public Map<String, Object> mapResponse() {
    		// mock数据
    		Map<String, Object> userInfos = new HashMap<>();
    		userInfos.put("username", "小明");
    		userInfos.put("age", 23);
    		userInfos.put("sex", "男");
    		userInfos.put("desc", "阳光乐观外向，喜欢唱歌、打篮球！");
    
    		// 返回数据
    		return userInfos;
    	}
    	
    	@GetMapping("listResponse")
    	@ApiOperation(value = "测试返回list型原始值", notes = "测试返回list型原始值")
    	public List<String> listResponse() {
    		// mock数据
    		List<String> list = new ArrayList<>();
    		list.add("first");
    		list.add("second");
    		list.add("third");
    		// 返回数据
    		return list;
    	}
    	
    	@GetMapping("customObjectResponse")
    	@ApiOperation(value = "测试返回自定义对象类型原始值", notes = "测试返回自定义对象类型原始值")
    	public CustomResponse customObjectResponse() {
    		// mock数据
    		CustomResponse resp = new CustomResponse();
    		resp.setUsername("小明")
    			.setAge(23)
    			.setSex(true)
    			.setDesc("阳光、帅气、活泼、积极、向上！");
    		// 返回数据
    		return resp;
    	}
    	
    	@GetMapping("wrappedResponse")
    	@ApiOperation(value = "测试返回已经被包装过的原始值", notes = "测试返回已经被包装过的原始值")
    	public DefaultResponseWrapper<List<String>> wrappedResponse() {
    		// mock数据
    		List<String> list = new ArrayList<>();
    		list.add("first");
    		list.add("second");
    		list.add("third");
    		// 返回数据
    		return new DefaultResponseWrapper<>(list);
    	}
    	
    }
    ```

* 源码`CustomResponse.java`

    ```java
    /**  
    * Title CustomResponse.java  
    * Description  
    * @author danyuan
    * @date Jan 3, 2021
    * @version 1.0.0
    * site: pddon.cn
    */ 
    package com.pddon.framework.demo.easyapi.controller.dto;
    
    import java.io.Serializable;
    
    import lombok.Data;
    import lombok.experimental.Accessors;
    
    @Data
    @Accessors(chain = true)
    public class CustomResponse implements Serializable{/** 
    	 *serialVersionUID
    	 */
    	private static final long serialVersionUID = 1L;
    	
    	private String username;
    	
    	private Integer age;
    	
    	private Boolean sex;
    	
    	private String desc;
    }
    ```

## 4 配置信息

> 我们还可以全局配置是否开启easyapi框架和默认是否自动对原始返回值进行包装。

```yaml
#easyapi:
  #全局默认配置
  #是否启用EasyApi框架
  #enable: false
  #是否强制对所有api响应添加响应壳
  #forceAutoAddResponseWrapper: false
```



