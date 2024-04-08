package com.pddon.framework.easyapi.client.apitools;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pddon.framework.easyapi.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: RequestParamParser
 * @Description: 解析请求
 * @Author: Allen
 * @Date: 2024-03-06 16:46
 * @Addr: https://pddon.cn
 */
@Slf4j
@Component
public class RequestParser {
    private static ObjectMapper mapper;

    @Autowired
    public RequestParser(ObjectMapper objectMapper){
        mapper = objectMapper;
    }
    public static Map<String, Object> parse(Object req){
        Map<String,Object> map = BeanUtil.beanToMap(req);
        return map;
    }

    public static URI parseToUrlParams(Map<String, Object> paramMap, String url){
        try{
            if (paramMap != null && paramMap.size() > 0) {
                if(url.indexOf("?") > 0){
                    url += '&';
                }else{
                    url += '?';
                }
                List<NameValuePair> params = new ArrayList<>();
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    if(entry.getValue() != null){
                        params.add(new BasicNameValuePair(entry.getKey(), URLEncoder.encode(entry.getValue().toString(), "UTF-8")));
                    }
                }
                String strParams = EntityUtils.toString(new UrlEncodedFormEntity(params));
                // 防止多参数时，分隔符","被转义
                String realParams = URLDecoder.decode(strParams, "UTF-8");
                url += realParams;
            }
            return new URI(url);
        }catch (Exception e){
            log.warn(e.getMessage());
            log.warn(IOUtils.getThrowableInfo(e));
        }
        return null;
    }

    public static HttpEntity parseToUrlEncodedEntity(Map<String, Object> paramMap, String url){
        try{
            // 设置参数
            if (paramMap != null && paramMap.size() > 0) {
                List<NameValuePair> params = new ArrayList<>();
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    if(entry.getValue() != null){
                        params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                    }
                }
                return new UrlEncodedFormEntity(params);
            }
        }catch (Exception e){
            log.warn(e.getMessage());
            log.warn(IOUtils.getThrowableInfo(e));
        }
        return null;
    }

    public static HttpEntity parseToStringEntity(Map<String, Object> paramMap, String url){
        try{
            // 设置参数
           // String data = JSONUtil.toJsonStr(paramMap);
            //ObjectMapper mapper = new ObjectMapper();
            //mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            String data = mapper.writeValueAsString(paramMap);
            return new StringEntity(data, "UTF-8");
        }catch (Exception e){
            log.warn(e.getMessage());
            log.warn(IOUtils.getThrowableInfo(e));
        }
        return null;
    }
}
