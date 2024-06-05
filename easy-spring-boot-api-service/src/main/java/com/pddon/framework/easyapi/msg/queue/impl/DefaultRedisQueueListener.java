package com.pddon.framework.easyapi.msg.queue.impl;

import com.pddon.framework.easyapi.config.EmailConfig;
import com.pddon.framework.easyapi.msg.queue.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @ClassName: DefaultRedisQueueListener
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-05 23:40
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class DefaultRedisQueueListener extends AbstractRedisQueueListener{

    @Autowired
    @Lazy
    private EmailConfig emailConfig;

    @Override
    public void onMessage(Message message) {
        if("emailConfigUpdated".equalsIgnoreCase(message.getType())){
            emailConfig.initDbConfig();
        }
    }
}
