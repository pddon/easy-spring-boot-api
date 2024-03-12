package com.pddon.framework.easyapi.client.apitools.impl;

import com.pddon.framework.easyapi.client.apitools.ApiPreHandler;
import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.client.config.SystemParameterRenameManager;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName: ApiSysParamSetPreHandler
 * @Description: 系统参数预设处理器
 * @Author: Allen
 * @Date: 2024-03-06 21:25
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class ApiSysParamSetPreHandler implements ApiPreHandler {

    @Override
    public int order() {
        return 999;//最后执行
    }

    @Override
    public Map<String, Object> handle(ApiInfo apiInfo, Map<String, Object> paramMap, ApplicationConfig config, Map<String, Object> headers) {
        paramMap.put(SystemParameterRenameManager.getSysParamName(SystemParameterRenameManager.APP_ID), config.getAppId());
        paramMap.put(SystemParameterRenameManager.getSysParamName(SystemParameterRenameManager.CHANNEL_ID), config.getChannelId());
        paramMap.put(SystemParameterRenameManager.getSysParamName(SystemParameterRenameManager.LOCALE), config.getLocale());
        headers.put("Accept", "application/json");
        return paramMap;
    }
}
