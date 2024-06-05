package com.pddon.framework.easyapi.msg.queue.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.msg.queue.Message;
import com.pddon.framework.easyapi.msg.queue.MessageManager;
import com.pddon.framework.easyapi.msg.queue.QueueListener;
import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @ClassName: AbstractRedisQueueListener
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-05 17:02
 * @Addr: https://pddon.cn
 */
@Slf4j
public abstract class AbstractRedisQueueListener implements QueueListener {

    @Autowired
    private RedisMessageListenerContainer container;

    @Autowired
    protected ObjectMapper objectMapper;

    @Override
    public String queue() {
        return MessageManager.EASY_API_DEFAULT_QUEUE;
    }

    @Override
    public void init() {
        QueueListener listener = this;
        container.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(org.springframework.data.redis.connection.Message message, byte[] bytes) {
                String msgBody = new String(message.getBody());
                try {
                    Message msg = objectMapper.readValue(msgBody, Message.class);
                    listener.onMessage(msg);
                } catch (JsonProcessingException e) {
                    listener.onError(null, e);
                }
            }
        }, new PatternTopic(this.queue()));
    }

    @Override
    public void onError(Message message, Exception e) {
        log.warn(IOUtils.getThrowableInfo(e));
    }
}
