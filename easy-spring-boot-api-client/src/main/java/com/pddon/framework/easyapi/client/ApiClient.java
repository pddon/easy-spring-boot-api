package com.pddon.framework.easyapi.client;

import com.pddon.framework.easyapi.client.apitools.*;
import com.pddon.framework.easyapi.client.apitools.*;
import com.pddon.framework.easyapi.client.config.ApplicationConfig;
import com.pddon.framework.easyapi.client.config.dto.ApiInfo;
import com.pddon.framework.easyapi.client.consts.HttpMethod;
import com.pddon.framework.easyapi.controller.response.DefaultResponseWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ApiClient
 * @Description: easyapi接口调用客户端
 * @Author: Allen
 * @Date: 2024-03-05 23:23
 * @Addr: https://pddon.cn
 */
@Data
@Component
@Slf4j
public class ApiClient implements ApplicationContextAware {

    /**
     * 配置信息
     */
    private ApplicationConfig config;

    private static List<ApiPreHandler> apiPreHandlers;

    private static List<ApiAfterHandler> apiAfterHandlers;

    private static List<HttpMethodHandler> httpMethodHandlers;

    public static ApiClient newInstance(ApplicationConfig config){
        ApiClient client = new ApiClient();
        client.setConfig(config);

        return client;
    }

    public <K,T> DefaultResponseWrapper<T> executeRequest(String apiName, HttpMethod method, K request, Class<T> tClass, boolean needSign, Map<String, Object> headers){
        return this.executeRequest(ApiInfo.builder()
                        .apiName(apiName)
                        .method(method)
                        .needSign(needSign)
                        .build(), request, tClass, headers);
    }

    public <K,T> DefaultResponseWrapper<T> executeRequest(ApiInfo apiInfo, K request, Class<T> tClass, Map<String, Object> headers){
        //参数解析
        Map<String, Object> paramsMap = RequestParser.parse(request);
        //请求参数预处理
        for(ApiPreHandler h : apiPreHandlers){
            paramsMap = h.handle(apiInfo, paramsMap, config, headers);
        }
        HttpMethodHandler handler = httpMethodHandlers.stream().filter(h -> h.support(apiInfo.getMethod())).findFirst().orElse(null);
        if(handler == null){
            log.warn("暂时不支持该类型请求！");
            return null;
        }
        if(apiInfo.getContentType() == null){
            apiInfo.setContentType("application/json");
        }
        CloseableHttpResponse httpResponse = handler.request(config.getBaseUrl() + apiInfo.getApiName(), paramsMap, headers, apiInfo.getContentType());
        String strData = ResponseParser.parse(httpResponse);
        if(strData != null){
            DefaultResponseWrapper<T> wrapper = ResponseParser.parseResponse(strData, tClass);
            if(wrapper.getData() != null && wrapper.isSuccess()){
                for(ApiAfterHandler h : apiAfterHandlers){
                    if(wrapper != null){
                        wrapper = h.handle(apiInfo, config, wrapper);
                    }
                }
            }
            return wrapper;
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //获取处理器列表
        try{
            apiPreHandlers = new ArrayList<>(applicationContext.getBeansOfType(ApiPreHandler.class).values());
        }catch (BeansException e){
            log.warn(e.getMessage());
        }
        try{
            apiAfterHandlers = new ArrayList<>(applicationContext.getBeansOfType(ApiAfterHandler.class).values());
        }catch (BeansException e){
            log.warn(e.getMessage());
        }
        try{
            httpMethodHandlers = new ArrayList<>(applicationContext.getBeansOfType(HttpMethodHandler.class).values());
        }catch (BeansException e){
            log.warn(e.getMessage());
        }
        //处理器排序
        apiPreHandlers.sort(Comparator.comparingInt(ApiPreHandler::order));
        apiAfterHandlers.sort(Comparator.comparingInt(ApiAfterHandler::order));
    }
}
