package com.pddon.framework.easyapi.swagger;

import com.google.common.collect.FluentIterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import springfox.documentation.service.Documentation;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.*;

/**
 * @ClassName: SwaggerResourcesExtProvider
 * @Description: 扩展功能，用来支持接口分组间排序
 * @Author: Allen
 * @Date: 2024-03-18 19:46
 * @Addr: https://pddon.cn
 */
@Component
@Qualifier("swaggerResourcesExtProvider")
public class SwaggerResourcesExtProvider  {
    private final String swagger3Url;

    boolean swagger3Available;

    private final DocumentationCache documentationCache;

    @Autowired
    public SwaggerResourcesExtProvider(Environment environment, DocumentationCache documentationCache) {
        swagger3Url = environment.getProperty("springfox.documentation.swagger.v3.path", "/v3/api-docs");
        swagger3Available = true;
        this.documentationCache = documentationCache;
    }

    public List<OrderedSwaggerResource> get() {
        List<OrderedSwaggerResource> resources = new ArrayList<OrderedSwaggerResource>();

        for (Map.Entry<String, Documentation> entry : documentationCache.all().entrySet()) {
            String swaggerGroup = entry.getKey();
            Documentation documentation=entry.getValue();
            List<VendorExtension> vendorExtensions=documentation.getVendorExtensions();

            if (swagger3Available) {
                OrderedSwaggerResource swaggerResource = resource(swaggerGroup, swagger3Url, vendorExtensions);
                swaggerResource.setSwaggerVersion("3.0");
                resources.add(swaggerResource);
            }
        }
        //根据自定义扩展属性order进行排序
        Collections.sort(resources, new Comparator<OrderedSwaggerResource>() {
            @Override
            public int compare(OrderedSwaggerResource o1, OrderedSwaggerResource o2) {
                return o1.getOrder().compareTo(o2.getOrder());
            }
        });
        return resources;
    }

    private OrderedSwaggerResource resource(String swaggerGroup, String baseUrl,List<VendorExtension> vendorExtensions) {
        OrderedSwaggerResource swaggerResource = new OrderedSwaggerResource();
        swaggerResource.setName(swaggerGroup);
        swaggerResource.setUrl(swaggerLocation(baseUrl, swaggerGroup));
        swaggerResource.setOrder(0);
        //判断是否不为空
        if (vendorExtensions!=null&&!vendorExtensions.isEmpty()){
            com.google.common.base.Optional<VendorExtension> ov= FluentIterable.from(vendorExtensions).filter(vendorExtension -> {
                return vendorExtension instanceof OrderExtension;
            }).first();
            if (ov.isPresent()){
                OrderExtension orderExtensions=(OrderExtension) ov.get();
                swaggerResource.setOrder(orderExtensions.getOrderValue());
            }
        }
        return swaggerResource;
    }

    private String swaggerLocation(String swaggerUrl, String swaggerGroup) {
        String base = Optional.of(swaggerUrl).get();
        if (Docket.DEFAULT_GROUP_NAME.equals(swaggerGroup)) {
            return base;
        }
        return base + "?group=" + swaggerGroup;
    }
}