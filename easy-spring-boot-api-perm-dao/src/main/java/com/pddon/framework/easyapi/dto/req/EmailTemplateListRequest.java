package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: EmailTemplateListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 22:04
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class EmailTemplateListRequest extends PaginationRequest {

    private String sceneId;
    /**
     * 邮件应用场景下特定的资源专属使用，通过新增sceneId的子分组下的字典，新增资源ID
     * @author pddon.com
     */
    private String resourceId;
    private String tenantId;
    /**
     * 所属应用ID
     * @author pddon.com
     */
    private String itemAppId;
    /**
     * 是否启用模板
     * @author pddon.com
     */
    private Boolean enabled;

    private String keyword;
}
