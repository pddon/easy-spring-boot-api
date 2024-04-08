package com.pddon.framework.easyapi.client.apitools;

import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;

/**
 * @ClassName: ApiAfterHandler
 * @Description: 接口调用后处理器
 * @Author: Allen
 * @Date: 2024-03-06 15:50
 * @Addr: https://pddon.cn
 */
public interface ApiAfterHandler {
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
     * 预处理响应信息
     * @param apiInfo
     * @param config
     * @param response
     * @return {@link DefaultResponseWrapper <T>}
     * @param <T>
     * @author: Allen
     * @Date: 2024/3/6 16:33
     */
    <T> DefaultResponseWrapper<T> handle(ApiInfo apiInfo, ApplicationConfig config, DefaultResponseWrapper<T> response);
}
