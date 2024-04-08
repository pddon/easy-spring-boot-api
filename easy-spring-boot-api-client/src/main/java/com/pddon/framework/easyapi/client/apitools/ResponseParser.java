package com.pddon.framework.easyapi.client.apitools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @ClassName: ResponseParser
 * @Description: 解析响应内容
 * @Author: Allen
 * @Date: 2024-03-06 16:46
 * @Addr: https://pddon.cn
 */
@Slf4j
@Component
public class ResponseParser {


    private static ObjectMapper mapper;

    @Autowired
    public ResponseParser(ObjectMapper objectMapper){
        mapper = objectMapper;
    }

    public static <T> DefaultResponseWrapper<T> parseResponse(String data, Class<T> tClass){
        //ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();;
        //mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        DefaultResponseWrapper<T> wrapper = null;
        try {
            Type[] types = new Type[1];
            types[0] = tClass;
            final ParameterizedTypeImpl type = ParameterizedTypeImpl.make(DefaultResponseWrapper.class, types,
                    DefaultResponseWrapper.class.getDeclaringClass());
            TypeReference<DefaultResponseWrapper> typeReference = new TypeReference<DefaultResponseWrapper>() {
                @Override
                public Type getType() {
                    return type;
                }
            };
            wrapper = mapper.readValue(data, typeReference);
        } catch (JsonProcessingException e) {
            log.warn(IOUtils.getThrowableInfo(e));
            throw new BusinessException("反序列化响应数据失败!");
        }
        /*JSONObject json = JSONUtil.parseObj(data);
        DefaultResponseWrapper<T> wrapper = json.toBean(DefaultResponseWrapper.class);
        if(json.containsKey("data")){
            T response = JSONUtil.parseObj(json.get("data")).toBean(tClass);
            wrapper.setData(response);
        }*/
        return wrapper;
    }

    public static String parse(CloseableHttpResponse response){
        String result = "{}";
        // 获取响应体
        HttpEntity httpEntity = null;
        try {
            // 获取响应体
            httpEntity = response.getEntity();
            if (httpEntity !=null) {
                result = EntityUtils.toString(httpEntity);
            }
            // 获取响应状态
            int statusCode = response.getStatusLine().getStatusCode();
            // 没有正常响应
            if (statusCode < HttpStatus.SC_OK || statusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
                throw new BusinessException("statusCode : " + statusCode + ", msg: " + result);
            }
            log.info("response data: {}", result);
        } catch (BusinessException e){
            throw e;
        }catch (Exception e) {
            log.error(IOUtils.getThrowableInfo(e));
            throw new BusinessException(e.getMessage());
        } finally {
            // 如果httpEntity没有被完全消耗，那么连接无法安全重复使用，将被关闭并丢弃
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException e) {
                log.warn(IOUtils.getThrowableInfo(e));
            }
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.warn(IOUtils.getThrowableInfo(e));
            }
        }
        return result;
    }
}
