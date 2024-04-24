package com.pddon.framework.easyapi.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName: ApplicationInitListener
 * @Description: 应用初始化成功，但是还未实例化任何
 * @Author: Allen
 * @Date: 2024-04-24 13:38
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class ApplicationInitListener implements ApplicationListener<ApplicationContextInitializedEvent> {
    @Autowired
    private List<ApplicationLifeListener> listeners;

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        listeners.forEach(listener -> listener.contextInitialized(event));
    }
}
