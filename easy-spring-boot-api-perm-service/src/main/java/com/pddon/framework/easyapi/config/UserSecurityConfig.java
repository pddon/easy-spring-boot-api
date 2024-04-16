package com.pddon.framework.easyapi.config;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.EasyApiCacheSessionDAO;
import com.pddon.framework.easyapi.UserAuthorizingRealm;
import com.pddon.framework.easyapi.filter.UserAuthenticatingFilter;
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
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName: UserSecurityConfig
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-16 00:17
 * @Addr: https://pddon.cn
 */
@Configuration
public class UserSecurityConfig {

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
    public SecurityManager securityManager(UserAuthorizingRealm userAuthorizingRealm, SessionManager shiroSessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userAuthorizingRealm);
        securityManager.setSessionManager(shiroSessionManager);

        return securityManager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shirFilter(@Autowired SecurityManager securityManager, @Autowired com.pddon.framework.easyapi.SessionManager sessionManager) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        //oauth过滤
        Map<String, Filter> filters = new HashMap<>(16);
        filters.put("userAuthFilter", new UserAuthenticatingFilter(sessionManager));
        shiroFilter.setFilters(filters);

        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/druid/**", "anon");
        filterMap.put("/login", "anon");

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
        filterMap.put("/**", "userAuthFilter");
        shiroFilter.setFilterChainDefinitionMap(filterMap);

        return shiroFilter;
    }

    @Bean
    public FilterRegistrationBean<DelegatingFilterProxy> shiroFilterRegistration() {
        FilterRegistrationBean<DelegatingFilterProxy> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DelegatingFilterProxy("shiroFilter"));
        //该值缺省为false，表示生命周期由SpringApplicationContext管理，设置为true则表示由ServletContainer管理
        registration.addInitParameter("targetFilterLifecycle", "true");
        registration.setEnabled(true);
        registration.setOrder(Integer.MAX_VALUE - 1);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
