package com.pddon.framework.easyapi.msg.queue.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.msg.queue.Message;
import com.pddon.framework.easyapi.msg.queue.QueueSender;
import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @ClassName: AbstractRedisQueueSender
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-05 16:52
 * @Addr: https://pddon.cn
 */
@Slf4j
public abstract class AbstractRedisQueueSender implements QueueSender {

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;
    @Autowired
    protected ObjectMapper objectMapper;

    @Override
    public boolean sendMessage(Message msg) {
        try{
            String strMsg = objectMapper.writeValueAsString(msg);
            redisTemplate.convertAndSend(this.queue(), strMsg);
            return true;
        }catch (Exception e){
            log.warn(IOUtils.getThrowableInfo(e));
        }
        return false;
    }

    @Override
    public boolean sendMessage(String content) {
        return this.sendMessage(Message.builder().content(content).build());
    }
}
