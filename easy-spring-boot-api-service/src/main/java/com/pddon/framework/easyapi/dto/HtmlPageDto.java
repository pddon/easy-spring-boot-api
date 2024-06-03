package com.pddon.framework.easyapi.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @ClassName: HtmlPageDto
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-03 21:37
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class HtmlPageDto implements Serializable {
    /**
     * 页面所属业务场景，字典分组pageBusinessId下的子分组列表，可以通过子字典分组新增业务应用场景
     * @author pddon.com
     */
    private String pageBusinessId;
    /**
     * 页面所属场景下特定的资源专属使用，通过新增pageBusinessId的子分组下的字典，新增资源ID
     * @author pddon.com
     */
    private String resourceId;
    /**
     * 页面访问路径，相对于服务器静态资源部署的根路径地址
     * @author pddon.com
     */
    private String urlPath;
    /**
     * 页面标题
     * @author pddon.com
     */
    private String title;
    /**
     * 页面关键字，以英文逗号分隔
     * @author pddon.com
     */
    private String keywords;
    /**
     * 描述
     * @author pddon.com
     */
    private String description;
}
