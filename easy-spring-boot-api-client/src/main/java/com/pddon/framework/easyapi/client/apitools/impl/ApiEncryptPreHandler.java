package com.pddon.framework.easyapi.client.apitools.impl;

import com.pddon.framework.easyapi.client.ClientDataEncryptHandler;
import com.pddon.framework.easyapi.client.apitools.ApiPreHandler;
import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName: ApiEncryptPreHandler
 * @Description: 请求参数加密处理器
 * @Author: Allen
 * @Date: 2024-03-06 21:25
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class ApiEncryptPreHandler implements ApiPreHandler {

    @Autowired
    private ClientDataEncryptHandler clientSignEncryptHandler;

    @Override
    public int order() {
        return -1;//最先执行
    }

    @Override
    public Map<String, Object> handle(ApiInfo apiInfo, Map<String, Object> paramMap, ApplicationConfig config, Map<String, Object> headers) {
        if(!apiInfo.isEncryptRequest()){
            return paramMap;
        }
        apiInfo.getEncryptParams().forEach(paramKey -> {
            Object value = paramMap.get(paramKey);
            if(value instanceof String){
                //字符串类型才可以加密
                try {
                    //String data = EncryptUtils.encodeAES128(config.getSecret(), value.toString());
                    String data = clientSignEncryptHandler.encrypt(config.getAppId(), config.getChannelId(), value.toString());
                    paramMap.put(paramKey, data);
                } catch (Exception e) {
                    log.warn("参数{}加密失败!", value);
                } catch (Throwable e) {
                    log.warn("参数{}加密失败!", value);
                }
            }
        });
        return paramMap;
    }
}
