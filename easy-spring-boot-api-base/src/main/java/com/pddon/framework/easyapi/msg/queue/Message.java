package com.pddon.framework.easyapi.msg.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.utils.IOUtils;
import com.pddon.framework.easyapi.utils.SpringBeanUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * @ClassName: Message
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-05 11:21
 * @Addr: https://pddon.cn
 */
@Data
@Builder
@Slf4j
public class Message implements Serializable {
    private String from;
    private String to;
    private String type;
    private String content;

    public <T> T getTypeContent(Class<T> tClass){
        ObjectMapper objectMapper = SpringBeanUtil.getBean(ObjectMapper.class);
        try {
            return objectMapper.readValue(content, tClass);
        } catch (JsonProcessingException e) {
            log.warn(IOUtils.getThrowableInfo(e));
        }
        return null;
    }

    public <T> T getTypeContent(Class<T> tClass, Type[] types){
        ObjectMapper objectMapper = SpringBeanUtil.getBean(ObjectMapper.class);
        try {
            final ParameterizedTypeImpl type = ParameterizedTypeImpl.make(tClass, types, null);
            TypeReference<T> typeReference = new TypeReference<T>() {
                @Override
                public Type getType() {
                    return type;
                }
            };
            return objectMapper.readValue(content, typeReference);
        } catch (JsonProcessingException e) {
            log.warn(IOUtils.getThrowableInfo(e));
        }
        return null;
    }
}
