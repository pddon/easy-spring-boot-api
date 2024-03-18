package com.pddon.framework.easyapi.swagger;

import lombok.Data;
import springfox.documentation.swagger.web.SwaggerResource;

/**
 * @ClassName: OrderedSwaggerResource
 * @Description:
 * @Author: Allen
 * @Date: 2024-03-18 19:45
 * @Addr: https://pddon.cn
 */
@Data
public class OrderedSwaggerResource extends SwaggerResource {
    private Integer order;
}
