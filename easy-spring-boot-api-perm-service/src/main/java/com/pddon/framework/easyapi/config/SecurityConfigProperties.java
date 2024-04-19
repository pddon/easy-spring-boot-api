package com.pddon.framework.easyapi.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
@ConfigurationProperties(prefix = "easyapi.security")
@Component
public class SecurityConfigProperties implements Serializable {

    /**
     * 不需要权限控制访问的资源url列表，支持通配符
     */
    private String[] annoAccessResourceUrls = {};
    /**
     * 需要账号认证成功后才能访问的资源列url表，支持通配符
     */
    private String[] authAccessResourceUrls = {};
    /**
     * 访问资源需要的权限配置，支持单个资源配置多个权限
     */
    private Map<String, String[]> resourcePerms = new HashMap<>();
    /**
     * 当无权限时跳转的页面
     */
    private String loginUrl = "/login.html";
    /**
     * 当无权限时跳转的页面
     */
    private String unauthorizedUrl = "/unauthorized.html";
    /**
     * 成功时跳转的页面
     */
    private String successUrl = "/index.html";
    /**
     * 会话最大存活时间，秒，默认1天过期，为负数时则代表会话不过期
     */
    private Integer sessionLiveTimeSeconds = 24 * 60 * 60;
    /**
     * 认证结果存储最大时长，默认7小时有效
     */
    private Integer rememberMeSeconds = 7 * 60 * 60;
    /**
     * 是否缓存用户权限信息
     */
    private boolean cacheable = true;


    /**
     * 会话是否需要过期
     * @return {@link boolean}
     * @author: Allen
     * @Date: 2024/4/19 17:14
     */
    @JsonIgnore
    public boolean sessionCanExpire(){
        return this.sessionLiveTimeSeconds > 0;
    }
}
