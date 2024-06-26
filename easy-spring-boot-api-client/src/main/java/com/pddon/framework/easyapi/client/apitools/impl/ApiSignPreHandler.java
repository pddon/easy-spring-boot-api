package com.pddon.framework.easyapi.client.apitools.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.pddon.framework.easyapi.client.ClientSignEncryptHandler;
import com.pddon.framework.easyapi.client.apitools.ApiPreHandler;
import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import com.pddon.framework.easyapi.utils.EncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName: ApiSignPreHandler
 * @Description: 请求参数加签处理器
 * @Author: Allen
 * @Date: 2024-03-06 21:43
 * @Addr: https://pddon.cn
 */
@Component
public class ApiSignPreHandler implements ApiPreHandler {

    @Autowired
    private ClientSignEncryptHandler clientSignEncryptHandler;

    @Override
    public int order() {
        //最先执行加签
        return -2;
    }

    @Override
    public Map<String, Object> handle(ApiInfo apiInfo, Map<String, Object> paramMap, ApplicationConfig config, Map<String, Object> headers) {
        if(!apiInfo.isNeedSign()){
            return paramMap;
        }
        Map<String,String> nameValueMap = new HashMap<>();
        for(String key : paramMap.keySet()){
            nameValueMap.putAll(BeanPropertyUtil.objToStringMap(paramMap.get(key), key));
        }
        if(CollectionUtil.isNotEmpty(apiInfo.getIgnoreSignParams())){
            Map<String, String> map = nameValueMap;
            nameValueMap = nameValueMap.keySet().stream()
                    .filter(key -> !apiInfo.getIgnoreSignParams().contains(key))
                    .collect(Collectors.toMap(key -> key, key -> map.get(key)));
        }
        //剔除系统参数
        final Map<String, String> map = nameValueMap;
        nameValueMap = nameValueMap.keySet().stream()
                .filter(key -> !SystemParameterRenameProperties.DEFAULT_PARAM_MAP.containsKey(key))
                .collect(Collectors.toMap(key->key, key -> map.get(key)));
        //按key进行字符串自然序排序后，进行拼接
        String content = EncryptUtils.sortAndMontage(nameValueMap);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String data = timestamp + content + timestamp;
        String sign = clientSignEncryptHandler.sign(config.getSecret(), data);
        paramMap.put(SystemParameterRenameProperties.getSysParamName(SystemParameterRenameProperties.SIGN), sign);
        paramMap.put(SystemParameterRenameProperties.getSysParamName(SystemParameterRenameProperties.TIMESTAMP), timestamp);
        return paramMap;
    }
}
