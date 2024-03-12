package com.pddon.framework.easyapi.client.apitools.impl;

import cn.hutool.json.JSONUtil;
import com.pddon.framework.easyapi.client.apitools.HttpMethodHandler;
import com.pddon.framework.easyapi.client.apitools.RequestParser;
import com.pddon.framework.easyapi.client.consts.HttpMethod;
import com.pddon.framework.easyapi.client.exception.BusinessException;
import com.pddon.framework.easyapi.client.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName: HttpPatchHandler
 * @Description: Patch请求处理器
 * @Author: Allen
 * @Date: 2024-03-06 21:04
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
public class HttpPatchHandler implements HttpMethodHandler {

    @Resource
    @Lazy
    private RequestConfig requestConfig;

    @Resource
    @Lazy
    private CloseableHttpClient httpClient;
    @Override
    public boolean support(HttpMethod method) {
        return HttpPatch.METHOD_NAME.equalsIgnoreCase(method.getValue());
    }

    @Override
    public CloseableHttpResponse request(String url, Map<String, Object> paramMap, Map<String, Object> headers, String contentType) {
        // 创建一个request对象
        HttpPatch httpPatch = new HttpPatch(url);
        CloseableHttpResponse response = null;
        try {
            // 配置连接参数
            httpPatch.setConfig(requestConfig);
            httpPatch.setHeader("Content-Type", contentType);
            // 设置参数
            httpPatch.setEntity(RequestParser.parseToStringEntity(paramMap, url));
            // 设置头
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, Object> entry : headers.entrySet()) {
                    if(entry.getValue() != null){
                        httpPatch.addHeader(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
            // 执行request请求
            response = httpClient.execute(httpPatch);
        } catch (Exception e) {
            String msg = "url : "+ httpPatch.getURI().toString() + ", headers: " + JSONUtil.toJsonStr(httpPatch.getAllHeaders()) +", params: " + JSONUtil.toJsonStr(paramMap) +", msg : " + e.getMessage();
            log.error(msg);
            log.error(IOUtils.getThrowableInfo(e));
            httpPatch.abort();
            throw new BusinessException(msg);
        }
        return response;
    }
}
