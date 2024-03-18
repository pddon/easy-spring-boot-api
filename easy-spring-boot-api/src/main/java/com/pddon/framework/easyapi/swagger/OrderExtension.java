package com.pddon.framework.easyapi.swagger;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import springfox.documentation.service.VendorExtension;

/**
 * @ClassName: OrderExtension
 * @Description:
 * @Author: Allen
 * @Date: 2024-03-18 19:53
 * @Addr: https://pddon.cn
 */
@AllArgsConstructor
@NoArgsConstructor
public class OrderExtension implements VendorExtension {

    private Integer value;

    @Override
    public String getName() {
        return "order";
    }

    @Override
    public Object getValue() {
        return value;
    }

    public Integer getOrderValue(){
        return value;
    }
}
