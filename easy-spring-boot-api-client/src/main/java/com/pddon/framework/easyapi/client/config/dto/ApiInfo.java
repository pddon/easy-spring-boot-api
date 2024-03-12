package com.pddon.framework.easyapi.client.config.dto;

import com.pddon.framework.easyapi.client.consts.HttpMethod;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * @ClassName: ApiInfo
 * @Description: 接口信息
 * @Author: Allen
 * @Date: 2024-03-06 15:52
 * @Addr: https://pddon.cn
 */
@Data
@Builder
public class ApiInfo implements Serializable {
    /**
     * 接口名，如：user/login.do
     */
    private String apiName;
    /**
     * 请求方法，如：GET 、 POST等等
     */
    private HttpMethod method;
    /**
     * 是否需要验签
     */
    private boolean needSign;
    /**
     * 是否需要对服务器返回结果验签
     */
    private boolean needSignResult;
    /**
     * 是否加密请求参数
     */
    private boolean encryptRequest;
    /**
     * 是否解密响应
     */
    private boolean decryptResponse;
    /**
     * 需要加密的参数列表
     */
    private Set<String> encryptParams;
    /**
     * 需要解密的参数列表
     */
    private Set<String> decryptParams;
    /**
     * 需要忽略验签的参数列表
     */
    private Set<String> ignoreSignParams;
    /**
     * 请求类型
     */
    private String contentType;

}
