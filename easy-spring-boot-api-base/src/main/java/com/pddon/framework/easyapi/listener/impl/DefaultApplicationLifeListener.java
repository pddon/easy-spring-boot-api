package com.pddon.framework.easyapi.listener.impl;

import com.pddon.framework.easyapi.listener.ApplicationLifeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.stereotype.Component;

/**
 * @ClassName: DefaultApplicationLifeListener
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-25 02:22
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class DefaultApplicationLifeListener implements ApplicationLifeListener {
    @Override
    public void contextInitialized(ApplicationContextInitializedEvent event) {
        if(log.isTraceEnabled()){
            log.trace("Application contextInitialized.");
        }
    }

    @Override
    public void started(ApplicationStartedEvent event) {
        if(log.isTraceEnabled()){
            log.trace("Application started.");
        }
    }

    @Override
    public void failed(ApplicationFailedEvent event) {
        if(log.isTraceEnabled()){
            log.trace("Application run failed.");
        }
    }
}
