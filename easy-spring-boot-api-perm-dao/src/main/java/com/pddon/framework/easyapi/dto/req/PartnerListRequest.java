package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: PartnerListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 22:04
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class PartnerListRequest extends PaginationRequest {
    private String tenantId;

    private String keyword;
}
