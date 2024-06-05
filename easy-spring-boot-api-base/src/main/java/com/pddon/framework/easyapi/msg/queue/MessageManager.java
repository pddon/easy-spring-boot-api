package com.pddon.framework.easyapi.msg.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName: MessageManager
 * @Description: 消息管理器
 * @Author: Allen
 * @Date: 2024-06-05 11:56
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class MessageManager {

    public final static String EASY_API_DEFAULT_QUEUE = "EASY_API_DEFAULT_QUEUE";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private List<QueueSender> senders;

    @Autowired(required = false)
    private List<QueueListener> listeners;

    public boolean sendMessage(String queue, String content){
        if(senders == null || senders.isEmpty()){
            log.warn("消息发送器未配置，无法发送消息!");
            return false;
        }
        List<QueueSender> matchedSenders = senders.stream().filter(sender -> sender.queue().equalsIgnoreCase(queue)).collect(Collectors.toList());
        return matchedSenders.stream().allMatch(sender -> {
            return sender.sendMessage(content);
        });
    }

    public boolean sendMessage(String queue, Message msg){
        if(senders == null || senders.isEmpty()){
            log.warn("消息发送器未配置，无法发送消息!");
            return false;
        }
        List<QueueSender> matchedSenders = senders.stream().filter(sender -> sender.queue().equalsIgnoreCase(queue)).collect(Collectors.toList());
        return matchedSenders.stream().allMatch(sender -> {
            return sender.sendMessage(msg);
        });
    }

    public boolean sendDefaultMessage(Message msg){
        return this.sendMessage(EASY_API_DEFAULT_QUEUE, msg);
    }

    public boolean sendDefaultMessage(String content){
        return this.sendMessage(EASY_API_DEFAULT_QUEUE, content);
    }

    @PostConstruct
    public void init(){
        if(this.listeners == null || this.listeners.isEmpty()){
            return;
        }
        this.listeners.forEach(QueueListener::init);
    }

}
