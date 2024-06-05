package com.pddon.framework.easyapi.msg.queue;

/**
 * @ClassName: QueueSender
 * @Description: 消息发送器
 * @Author: Allen
 * @Date: 2024-06-05 11:39
 * @Addr: https://pddon.cn
 */
public interface QueueSender {

    /**
     * 需要监听的队列名
     * @return {@link String}
     * @author: Allen
     * @Date: 2024/6/5 11:55
     */
    String queue();

    /**
     * 发送消息
     * @param msg
     * @return {@link boolean}
     * @author: Allen
     * @Date: 2024/6/5 11:41
     */
    boolean sendMessage(Message msg);

    /**
     * 发送简单消息
     * @param content
     * @return {@link boolean}
     * @author: Allen
     * @Date: 2024/6/5 11:41
     */
    boolean sendMessage(String content);
}
