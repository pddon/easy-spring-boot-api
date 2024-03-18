package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.annotation.IgnoreResponseWrapper;
import com.pddon.framework.easyapi.annotation.ReplaceRoute;
import com.pddon.framework.easyapi.swagger.OrderedSwaggerResource;
import com.pddon.framework.easyapi.swagger.SwaggerResourcesExtProvider;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * @ClassName: SwaggerController
 * @Description: 替换swagger接口分组信息获取接口
 * @Author: Allen
 * @Date: 2024-03-18 20:03
 * @Addr: https://pddon.cn
 */
@Api(tags = "swagger相关接口")
@ApiIgnore
@Controller
@RequestMapping("custom")
//覆盖swagger默认接口
@ReplaceRoute
public class SwaggerController {
    private static final String HAL_MEDIA_TYPE = "application/json";
    private final SwaggerResourcesExtProvider swaggerResourcesExtProvider;

    @Autowired
    public SwaggerController(@Qualifier("swaggerResourcesExtProvider") SwaggerResourcesExtProvider swaggerResources) {
        this.swaggerResourcesExtProvider = swaggerResources;
    }

    @GetMapping(path = "swagger-resources",
            produces = {APPLICATION_JSON_VALUE, HAL_MEDIA_TYPE})
    @ResponseBody
    @IgnoreResponseWrapper
    public ResponseEntity<List<OrderedSwaggerResource>> swaggerResources() {
        return new ResponseEntity<>(swaggerResourcesExtProvider.get(), HttpStatus.OK);
    }
}
