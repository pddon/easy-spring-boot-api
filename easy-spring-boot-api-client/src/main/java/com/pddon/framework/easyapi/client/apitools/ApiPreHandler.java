package com.pddon.framework.easyapi.client.apitools;

import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;

import java.util.Map;

/**
 * @ClassName: ApiPreHandler
 * @Description: 接口调用预处理器
 * @Author: Allen
 * @Date: 2024-03-06 15:49
 * @Addr: https://pddon.cn
 */
public interface ApiPreHandler {
    /**
     * 执行顺序优先级
     * @return {@link int}
     * @author: Allen
     * @Date: 2024/3/6 16:33
     */
    default int order() {
        return 0;
    }

    /**
     * 处理请求参数
     * @param apiInfo
     * @param paramMap
     * @param config
     * @param headers
     * @return {@link Map<String, Object>}
     * @author: Allen
     * @Date: 2024/3/6 16:22
     */
    Map<String, Object> handle(ApiInfo apiInfo, Map<String, Object> paramMap, ApplicationConfig config, Map<String, Object> headers);
}
