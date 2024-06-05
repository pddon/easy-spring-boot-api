package com.pddon.framework.easyapi.msg.queue;

/**
 * @ClassName: QueueListener
 * @Description: 队列消息监听器
 * @Author: Allen
 * @Date: 2024-06-05 11:41
 * @Addr: https://pddon.cn
 */
public interface QueueListener {
    /**
     * 初始化
     * @author: Allen
     * @Date: 2024/6/6 0:50
     */
    void init();
    /**
     * 需要监听的队列名
     * @return {@link String}
     * @author: Allen
     * @Date: 2024/6/5 11:55
     */
    String queue();

    /**
     * 监听消息
     * @param message
     * @author: Allen
     * @Date: 2024/6/5 11:55
     */
    void onMessage(Message message);

    /**
     * 消息监听发生异常时的处理方法
     * @param message
     * @param e
     * @author: Allen
     * @Date: 2024/6/5 11:55
     */
    void onError(Message message, Exception e);
}
