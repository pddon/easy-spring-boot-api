package com.pddon.framework.easyapi.config;

import com.pddon.framework.easyapi.controller.utils.CustomRequestMappingHandlerMapping;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @ClassName: EasyApiRequestMappingConfig
 * @Description:
 * @Author: Allen
 * @Date: 2024-03-18 21:55
 * @Addr: https://pddon.cn
 */
@Component
public class EasyApiRequestMappingConfig implements WebMvcRegistrations {
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new CustomRequestMappingHandlerMapping();
        handlerMapping.setOrder(0);
        return handlerMapping;
    }
}
