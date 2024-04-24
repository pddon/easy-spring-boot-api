package com.pddon.framework.easyapi.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName: ApplicationFailedListener
 * @Description: 应用启动失败监听器
 * @Author: Allen
 * @Date: 2024-04-24 13:38
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class ApplicationFailedListener implements ApplicationListener<ApplicationFailedEvent> {
    @Autowired
    private List<ApplicationLifeListener> listeners;

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        listeners.forEach(listener -> listener.failed(event));
    }
}
