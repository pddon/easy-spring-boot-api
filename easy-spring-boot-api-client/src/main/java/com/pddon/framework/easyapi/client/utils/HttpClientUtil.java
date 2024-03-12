package com.pddon.framework.easyapi.client.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.pddon.framework.easyapi.client.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class HttpClientUtil implements ApplicationContextAware {
    
    private static CloseableHttpClient httpClient;

    private static RequestConfig requestConfig;

    public static <T> T get(String url, Object req, Map<String, Object> headers, Class<T> tClass){
        Map<String,Object> map = BeanUtil.beanToMap(req);
        headers.put("Accept", "application/json");
        String responseStr = get(url, map, headers);
        return JSONUtil.parseObj(responseStr).toBean(tClass);
    }

    public static <T> T post(String url, Object req, Map<String, Object> headers, Class<T> tClass){
        Map<String,Object> map = BeanUtil.beanToMap(req);
        headers.put("Accept", "application/json");
        String responseStr = post(url, map, headers);
        return JSONUtil.parseObj(responseStr).toBean(tClass);
    }

    public static String get(String url, Map<String, Object> paramMap, Map<String, Object> headers) {
        String result = null;
        if ("".equals(url)) {
            return result;
        }
        // 创建一个request对象
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            // 配置连接参数
            httpGet.setConfig(requestConfig);
            //设置参数
            if (paramMap != null && paramMap.size() > 0) {
                List<NameValuePair> params = new ArrayList<>();
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    if(entry.getValue() != null){
                        params.add(new BasicNameValuePair(entry.getKey(), URLEncoder.encode(entry.getValue().toString(), "UTF-8")));
                    }
                }
                String strParams = EntityUtils.toString(new UrlEncodedFormEntity(params));
                // 防止多参数时，分隔符","被转义
                String realParams = URLDecoder.decode(strParams, "UTF-8");
                httpGet.setURI(new URI(httpGet.getURI().toString().indexOf("?") > 0 ? httpGet.getURI().toString() + "&" + realParams : httpGet.getURI().toString() + "?" + realParams));
            }
            // 设置头
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    if(entry.getValue() != null){
                        httpGet.addHeader(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
            // 执行request请求
            response = httpClient.execute(httpGet);
            result = responseHandle(response);

        } catch (Exception e) {
            String msg = "url : "+ httpGet.getURI().toString() + ", headers: " + JSONUtil.toJsonStr(httpGet.getAllHeaders()) +", msg : " + e.getMessage();
            log.error(msg);
            log.error(IOUtils.getThrowableInfo(e));
            httpGet.abort();
            throw new BusinessException(msg);
        } finally {
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

    public static String post(String url, Map<String, Object> paramMap, Map<String, Object> headers) {
        String result = null;
        if ("".equals(url)) {
            return result;
        }
        // 创建一个request对象
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        try {
            // 配置连接参数
            httpPost.setConfig(requestConfig);
            // 设置参数
            if (paramMap != null && paramMap.size() > 0) {
                List<NameValuePair> params = new ArrayList<>();
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    if(entry.getValue() != null){
                        params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                    }
                }
                HttpEntity entity = new UrlEncodedFormEntity(params);
                httpPost.setEntity(entity);
            }
            // 设置头
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    if(entry.getValue() != null){
                        httpPost.addHeader(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
            // 执行request请求
            response = httpClient.execute(httpPost);
            result = responseHandle(response);
        } catch (Exception e) {
            String msg = "url : "+ httpPost.getURI().toString() + ", headers: " + JSONUtil.toJsonStr(httpPost.getAllHeaders()) +", params: " + JSONUtil.toJsonStr(paramMap) +", msg : " + e.getMessage();
            log.error(msg);
            log.error(IOUtils.getThrowableInfo(e));
            httpPost.abort();
            throw new BusinessException(msg);
        } finally {
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

    public static String postJSON(String url, String json_str, HashMap<String, Object> headers) {
        String result = null;
        if ("".equals(url)) {
            return result;
        }
        // 创建一个request对象
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        try {
            // 配置连接参数
            httpPost.setConfig(requestConfig);
            // 设置参数
            if (json_str != null && !"".equals(json_str)) {
                StringEntity entity = new StringEntity(json_str, ContentType.APPLICATION_JSON);
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }
            // 设置头
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue().toString());
                }
            }
            // 执行request请求
            response = httpClient.execute(httpPost);
            result = responseHandle(response);

        } catch (Exception e) {
            log.error("url : "+ url +", msg : " + e.getMessage()+", param : " +json_str);
            httpPost.abort();
        } finally {
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

    private static String responseHandle(CloseableHttpResponse response) throws IOException {
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
        } finally {
            // 如果httpEntity没有被完全消耗，那么连接无法安全重复使用，将被关闭并丢弃
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException e) {
                log.warn(IOUtils.getThrowableInfo(e));
            }
        }
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //初始化
        httpClient = applicationContext.getBean(CloseableHttpClient.class);
        requestConfig = applicationContext.getBean(RequestConfig.class);
    }
}