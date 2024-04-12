package com.pddon.framework.easyapi.client;

/**
 * @ClassName: ClientDataEncryptHandler
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-12 10:46
 * @Addr: https://pddon.cn
 */
public interface ClientDataEncryptHandler {
    String encrypt(String appId, String channelId, String content) throws Throwable ;
    String decrypt(String appId, String channelId, String content) throws Throwable ;
}
