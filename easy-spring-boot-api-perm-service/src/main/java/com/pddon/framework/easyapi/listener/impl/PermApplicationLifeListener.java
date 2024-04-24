package com.pddon.framework.easyapi.listener.impl;

import com.pddon.framework.easyapi.UserSecurityService;
import com.pddon.framework.easyapi.listener.ApplicationLifeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.stereotype.Component;

/**
 * @ClassName: PermApplicationLifeListener
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-24 14:36
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class PermApplicationLifeListener implements ApplicationLifeListener {

    @Autowired
    private UserSecurityService userSecurityService;

    @Override
    public void contextInitialized(ApplicationContextInitializedEvent event) {

    }

    @Override
    public void started(ApplicationStartedEvent event) {
        userSecurityService.checkAndCreateSuperManager();
    }

    @Override
    public void failed(ApplicationFailedEvent event) {

    }
}
