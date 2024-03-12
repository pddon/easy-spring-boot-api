package com.pddon.framework.easyapi.client.request;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * @ClassName: PrePayRequest
 * @Description: 支付预下单请求参数
 * @Author: Allen
 * @Date: 2024-03-02 23:17
 * @Addr: https://pddon.cn
 */
@Data
@Builder
@Accessors(chain = true)
public class PrePayRequest implements Serializable {
    private Integer totalAmount;
    private CurrencyType currency;
    private String description;
    private String notifyUrl;
    private String appId;
    private String userId;
    private String userNickname;
    private String orderId;
    private Integer originalAmount;
    private String productInfo;
    private Map<String, Object> extParams;
}
