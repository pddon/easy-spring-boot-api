package com.pddon.framework.easyapi.client.consts;

import lombok.Getter;
import org.apache.http.client.methods.*;

/**
 * @ClassName: HttpMethod
 * @Description: 常用Http请求类型
 * @Author: Allen
 * @Date: 2024-03-06 21:13
 * @Addr: https://pddon.cn
 */
@Getter
public enum HttpMethod {
    GET(HttpGet.METHOD_NAME),
    POST(HttpPost.METHOD_NAME),
    PUT(HttpPut.METHOD_NAME),
    DELETE(HttpDelete.METHOD_NAME),
    HEAD(HttpHead.METHOD_NAME),
    PATCH(HttpPatch.METHOD_NAME);

    private String value;

    HttpMethod(String value){
        this.value = value;
    }
}
