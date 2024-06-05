package com.pddon.framework.easyapi.msg.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private List<QueueSender> senders;

    @Autowired
    private List<QueueListener> listeners;

    public boolean sendMessage(String queue, String content){
        List<QueueSender> matchedSenders = senders.stream().filter(sender -> sender.queue().equalsIgnoreCase(queue)).collect(Collectors.toList());
        return matchedSenders.stream().allMatch(sender -> {
            return sender.sendMessage(content);
        });
    }

    public boolean sendMessage(String queue, Message msg){
        List<QueueSender> matchedSenders = senders.stream().filter(sender -> sender.queue().equalsIgnoreCase(queue)).collect(Collectors.toList());
        return matchedSenders.stream().allMatch(sender -> {
            return sender.sendMessage(msg);
        });
    }
}
