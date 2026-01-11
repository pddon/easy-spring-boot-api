package com.pddon.framework.easyapi.client.annotations;

import com.pddon.framework.easyapi.client.config.ClientConfigurer;
import com.pddon.framework.easyapi.client.config.ClientHttpClientConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @ClassName: EnableEasyApiClient
 * @Description: 启用easyapi客户端服务
 * @Author: Allen
 * @Date: 2024-03-05 23:37
 * @Addr: https://pddon.cn
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ClientHttpClientConfig.class, ClientConfigurer.class})
public @interface EnableEasyApiClient {
}
