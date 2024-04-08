package com.pddon.framework.easyapi.client.apitools.impl;

import com.pddon.framework.easyapi.client.apitools.ApiAfterHandler;
import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: ApiCheckSignAfterHandler
 * @Description: 验签拦截器
 * @Author: Allen
 * @Date: 2024-03-06 22:17
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class ApiCheckSignAfterHandler implements ApiAfterHandler {
    @Override
    public int order() {
        return -2;//验签处理器最先执行
    }

    @Override
    public <T> DefaultResponseWrapper<T> handle(ApiInfo apiInfo, ApplicationConfig config, DefaultResponseWrapper<T> response) {
        if(!apiInfo.isNeedSignResult()){
            return response;
        }
        if(response.getData() == null){
            return response;
        }
        T data = response.getData();
        Map<String,String> nameValueMap = new HashMap<>();
        nameValueMap.putAll(BeanPropertyUtil.objToStringMap(data, null));
        //按key进行字符串自然序排序后，进行拼接
        String content = EncryptUtils.sortAndMontage(nameValueMap);
        String timestamp = response.getTimestamp() != null ? response.getTimestamp().toString() : "";
        content = timestamp + content + timestamp;
        String sign = EncryptUtils.encryptSHA1Hex(config.getSecret(), content);
        if(!sign.equalsIgnoreCase(response.getSign())){
            log.warn("content: {}", content);
            throw new BusinessException(ErrorCodes.ERROR_SIGN.getCode(), ErrorCodes.ERROR_SIGN.getMsgCode());
        }
        return response;
    }
}
