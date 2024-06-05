package com.pddon.framework.easyapi.msg.queue.impl;

import com.pddon.framework.easyapi.msg.queue.MessageManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName: DefaultRedisQueueSender
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-05 16:52
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class DefaultRedisQueueSender extends AbstractRedisQueueSender {
    @Override
    public String queue() {
        return MessageManager.EASY_API_DEFAULT_QUEUE;
    }
}
