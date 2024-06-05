package com.pddon.framework.easyapi.msg.queue.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.msg.queue.Message;
import com.pddon.framework.easyapi.msg.queue.QueueSender;
import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
public abstract class DefaultRedisQueueSender extends AbstractRedisQueueSender {
    @Override
    public String queue() {
        return "EASY_API_DEFAULT_QUEUE";
    }
}
