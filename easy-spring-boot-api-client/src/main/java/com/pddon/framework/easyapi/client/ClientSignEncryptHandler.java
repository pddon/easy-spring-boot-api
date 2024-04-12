package com.pddon.framework.easyapi.client;

/**
 * @ClassName: ClientSignEncryptHandler
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-12 10:52
 * @Addr: https://pddon.cn
 */
public interface ClientSignEncryptHandler {
    String sign(String secret, String body);
}
