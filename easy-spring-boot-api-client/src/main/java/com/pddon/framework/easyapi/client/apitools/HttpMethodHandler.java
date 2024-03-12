package com.pddon.framework.easyapi.client.apitools;

import com.pddon.framework.easyapi.client.consts.HttpMethod;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.util.Map;

/**
 * @ClassName: HttpMethodHandler
 * @Description: 请求处理器
 * @Author: Allen
 * @Date: 2024-03-06 17:46
 * @Addr: https://pddon.cn
 */
public interface HttpMethodHandler {

    /**
     * 是否支持
     * @param method
     * @return {@link boolean}
     * @author: Allen
     * @Date: 2024/3/6 21:16
     */
    boolean support(HttpMethod method);

    /**
     * 发起请求
     * @param url
     * @param paramMap
     * @param headers
     * @param contentType
     * @return {@link CloseableHttpResponse}
     * @author: Allen
     * @Date: 2024/3/6 17:51
     */
    CloseableHttpResponse request(String url, Map<String, Object> paramMap, Map<String, Object> headers, String contentType);
}
