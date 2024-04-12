package com.pddon.framework.easyapi.client;

import com.pddon.framework.easyapi.dto.SecretInfo;

/**
 * @ClassName: ClientSecretManager
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-12 10:42
 * @Addr: https://pddon.cn
 */
public interface ClientSecretManager {
    /**
     * 加载秘钥信息
     * @author danyuan
     */
    SecretInfo load(String channelId, String appId, String secretId);

}
