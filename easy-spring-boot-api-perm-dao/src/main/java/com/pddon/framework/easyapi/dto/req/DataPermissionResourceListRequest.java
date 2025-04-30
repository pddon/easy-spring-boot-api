package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: DataPermissionResourceListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 21:38
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class DataPermissionResourceListRequest extends PaginationRequest {

    private String tenantId;

    private String permId;

    private String keyword;
}
