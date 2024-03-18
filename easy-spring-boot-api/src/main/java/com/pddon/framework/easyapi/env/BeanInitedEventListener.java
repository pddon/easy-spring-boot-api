package com.pddon.framework.easyapi.env;

import com.pddon.framework.easyapi.controller.utils.ReplaceRouteUtil;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @ClassName: BeanInitedEventListener
 * @Description:
 * @Author: Allen
 * @Date: 2024-03-18 22:10
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class BeanInitedEventListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private EasyApiConfig easyApiConfig;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            // 父上下文不存在，即根上下文，可以在这里执行Bean都初始化后的逻辑
            // 例如，Controller都已初始化，可以在这里进行相关操作
            ReplaceRouteUtil.initRoute(easyApiConfig.getAllBasePackages().stream().collect(Collectors.toList()));
        }
    }
}
