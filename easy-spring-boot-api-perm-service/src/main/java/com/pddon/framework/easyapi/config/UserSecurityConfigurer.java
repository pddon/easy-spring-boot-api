package com.pddon.framework.easyapi.config;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.EasyApiCacheSessionDAO;
import com.pddon.framework.easyapi.UserAuthorizingRealm;
import com.pddon.framework.easyapi.filter.UserAuthenticatingFilter;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName: UserSecurityConfigurer
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-16 00:17
 * @Addr: https://pddon.cn
 */
@Configuration
public class UserSecurityConfigurer {

    @Bean
    public MapperScannerConfigurer permsMapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setBasePackage("com.pddon.framework.easyapi.dao.mapper");
        return configurer;
    }

    @Bean
    public EasyApiCacheSessionDAO sessionDAO(@Autowired com.pddon.framework.easyapi.SessionManager sessionManager, @Autowired CacheManager cacheManager) {
        return new EasyApiCacheSessionDAO(sessionManager, cacheManager);
    }

    @Bean("shiroSessionManager")
    public SessionManager shiroSessionManager(@Autowired EasyApiCacheSessionDAO sessionDAO) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setSessionDAO(sessionDAO);
        return sessionManager;
    }

    @Bean("securityManager")
    public SecurityManager securityManager(UserAuthorizingRealm userAuthorizingRealm, HashedCredentialsMatcher credentialsMatcher, SessionManager shiroSessionManager) {
        userAuthorizingRealm.setCredentialsMatcher(credentialsMatcher);
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userAuthorizingRealm);
        securityManager.setSessionManager(shiroSessionManager);

        return securityManager;
    }

    //因为我们的密码是加过密的，所以，如果要Shiro验证用户身份的话，需要告诉它我们用的是md5加密的，并且是加密了两次。同时我们在自己的Realm中也通过SimpleAuthenticationInfo返回了加密时使用的盐。这样Shiro就能顺利的解密密码并验证用户名和密码是否正确了。
    @Bean(name = "credentialsMatcher")
    public HashedCredentialsMatcher credentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("md5");//散列算法:这里使用MD5算法;
        hashedCredentialsMatcher.setHashIterations(1);//散列的次数，比如散列两次，相当于 md5(md5(""));
        return hashedCredentialsMatcher;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shirFilter(@Autowired SecurityConfigProperties securityConfigProperties, @Autowired SecurityManager securityManager, @Autowired com.pddon.framework.easyapi.SessionManager sessionManager) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        //oauth过滤
        Map<String, Filter> filters = new HashMap<>(16);
        filters.put("userAuthFilter", new UserAuthenticatingFilter(sessionManager));
        shiroFilter.setFilters(filters);

        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/druid/**", "anon");
        filterMap.put("/login", "anon");

        filterMap.put("/favicon.ico", "anon");
        filterMap.put("/doc.html", "anon");
        filterMap.put("/v3/api-docs", "anon");
        filterMap.put("/v3/api-docs-ext", "anon");
        filterMap.put("/swagger-ui/**", "anon");
        filterMap.put("/swagger-resources", "anon");
        filterMap.put("/webjars/**", "anon");
        filterMap.put("/assets/**", "anon");
        filterMap.put("/css/**", "anon");
        filterMap.put("/js/**", "anon");
        filterMap.put("/public/**", "anon");
        //无权限访问业务资源
        Arrays.stream(securityConfigProperties.getAnnoAccessResourceUrls()).forEach(url -> {
            filterMap.put(url, "anon");
        });
        //需要认证后才能访问的业务资源
        Arrays.stream(securityConfigProperties.getAuthAccessResourceUrls()).forEach(url -> {
            filterMap.put(url, "authc");
        });
        //需要特殊权限访问用户资源
        securityConfigProperties.getResourcePerms().forEach((url, permArr) -> {
            String perms = "perms[" + Arrays.stream(permArr).collect(Collectors.joining(",")) + "]";
            filterMap.put(url, perms);
        });
        filterMap.put("/**", "userAuthFilter");
        shiroFilter.setFilterChainDefinitionMap(filterMap);
        shiroFilter.setLoginUrl(securityConfigProperties.getLoginUrl());
        shiroFilter.setUnauthorizedUrl(securityConfigProperties.getUnauthorizedUrl());
        shiroFilter.setSuccessUrl(securityConfigProperties.getSuccessUrl());

        return shiroFilter;
    }

    @Bean
    public FilterRegistrationBean<DelegatingFilterProxy> shiroFilterRegistration() {
        FilterRegistrationBean<DelegatingFilterProxy> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DelegatingFilterProxy("shiroFilter"));
        //该值缺省为false，表示生命周期由SpringApplicationContext管理，设置为true则表示由ServletContainer管理
        registration.addInitParameter("targetFilterLifecycle", "true");
        registration.setEnabled(true);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
