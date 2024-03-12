package com.pddon.framework.easyapi.client.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @ClassName: ApplicationConfig
 * @Description: 应用配置
 * @Author: Allen
 * @Date: 2024-03-05 23:00
 * @Addr: https://pddon.cn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ConfigurationProperties(prefix = "easyapi.client.app")
@Component
public class ApplicationConfig implements Serializable {

    /**
     * 服务器接口根路径地址
     */
    private String baseUrl;
    /**
     * 渠道ID
     */
    private String channelId;
    /**
     * 应用ID
     */
    private String appId;

    private String locale;
    /**
     * 对称秘钥，用于加签、验签，对数据进行对称加解密
     */
    private String secret;
    /**
     * 非对称秘钥对，私钥解密
     */
    private String privateSecret;
    /**
     * 非对称秘钥对，公钥加密
     */
    private String publicSecret;
}
