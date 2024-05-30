package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import com.pddon.framework.easyapi.dao.consts.HtmlPageStatus;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: HtmlPageListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 22:04
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class HtmlPageListRequest extends PaginationRequest {

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
     * 页面状态 取值：EDIT 编辑中， COMPLETE 已完成， DEPLOYED 已部署， OFFLINE 已下线
     * @author pddon.com
     */
    private HtmlPageStatus pageStatus;

    private String keyword;
}
