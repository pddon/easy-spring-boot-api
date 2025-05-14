package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: DataPermissionListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 21:38
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class DataPermissionListRequest extends PaginationRequest {

    private String tenantId;

    private String keyword;

    /**
     * 取值： FIELD字段查询； TABLE_FIELD 表字段查询
     * @author pddon.com
     */
    private String queryType;

    private Boolean disabled;
}
