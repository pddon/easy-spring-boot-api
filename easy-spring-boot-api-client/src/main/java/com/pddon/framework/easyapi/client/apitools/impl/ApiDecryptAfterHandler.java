package com.pddon.framework.easyapi.client.apitools.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.pddon.framework.easyapi.client.ClientDataEncryptHandler;
import com.pddon.framework.easyapi.client.apitools.ApiAfterHandler;
import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @ClassName: ApiCheckSignAfterHandler
 * @Description: 解密处理器
 * @Author: Allen
 * @Date: 2024-03-06 22:17
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class ApiDecryptAfterHandler implements ApiAfterHandler {

    @Autowired
    private ClientDataEncryptHandler clientSignEncryptHandler;

    @Override
    public int order() {
        return 999;//解密处理器最后执行
    }

    @Override
    public <T> DefaultResponseWrapper<T> handle(ApiInfo apiInfo, ApplicationConfig config, DefaultResponseWrapper<T> response) {
        if(!apiInfo.isDecryptResponse() || CollectionUtil.isEmpty(apiInfo.getDecryptParams())){
            return response;
        }
        if(response.getData() == null){
            return response;
        }
        T obj = response.getData();
        try {
            obj = (T) BeanPropertyUtil.decorateObj("", obj, Collections.emptyMap(),  (fieldName, data, annotations)->{
                if(apiInfo.getDecryptParams().contains(fieldName)){
                    //data = EncryptUtils.decodeAES128(config.getSecret(), data.toString());
                    data = clientSignEncryptHandler.decrypt(config.getAppId(), config.getChannelId(), data.toString());
                }
                return data;
            });
        } catch (Throwable e) {
            log.warn(e.getMessage());
            log.warn(IOUtils.getThrowableInfo(e));
        }
        response.setData(obj);
        return response;
    }
}
