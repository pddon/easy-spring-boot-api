package com.pddon.framework.easyapi.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName: ApplicationStartedListener
 * @Description: 应用启动成功监听器
 * @Author: Allen
 * @Date: 2024-04-24 13:38
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {
    @Autowired
    private List<ApplicationLifeListener> listeners;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        listeners.forEach(listener -> listener.started(event));
    }
}
