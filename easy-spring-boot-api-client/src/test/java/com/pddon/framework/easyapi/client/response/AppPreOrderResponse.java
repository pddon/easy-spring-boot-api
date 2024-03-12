package com.pddon.framework.easyapi.client.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @ClassName: AppPreOrderResponse
 * @Description: 预下单返回信息
 * @Author: Allen
 * @Date: 2024-03-03 00:10
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AppPreOrderResponse implements Serializable {
    /**
     * 支付中心交易流水号
     */
    private String outTradeNo;
    /**
     * 业务系统订单号
     */
    private String orderId;
    /**
     * 支付收银台地址
     */
    private String redirectUrl;
}
