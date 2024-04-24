package com.pddon.framework.easyapi.listener;

import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;

/**
 * @ClassName: ApplicationLifeListener
 * @Description: EasyApi生命周期监听器
 * @Author: Allen
 * @Date: 2024-04-24 13:41
 * @Addr: https://pddon.cn
 */
public interface ApplicationLifeListener {
    void contextInitialized(ApplicationContextInitializedEvent event);

    void started(ApplicationStartedEvent event);

    void failed(ApplicationFailedEvent event);
}
