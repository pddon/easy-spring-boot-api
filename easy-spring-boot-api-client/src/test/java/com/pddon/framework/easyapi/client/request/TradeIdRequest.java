package com.pddon.framework.easyapi.client.request;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @ClassName: TradeIdRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-03-03 02:49
 * @Addr: https://pddon.cn
 */
@Data
@Builder
@Accessors(chain = true)
public class TradeIdRequest implements Serializable {

    private String appId;

    private String orderId;

    private String outTradeNo;
}
