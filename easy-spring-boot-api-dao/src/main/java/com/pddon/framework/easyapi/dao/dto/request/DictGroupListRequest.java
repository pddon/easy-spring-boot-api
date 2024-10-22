package com.pddon.framework.easyapi.dao.dto.request;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;

/**
 * @ClassName: DictGroupListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 22:04
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class DictGroupListRequest extends PaginationRequest {

    private String tenantId;
    private String groupId;
    /**
     * 父分组Id
     */
    private String parentGroupId;

    private String keyword;
}
