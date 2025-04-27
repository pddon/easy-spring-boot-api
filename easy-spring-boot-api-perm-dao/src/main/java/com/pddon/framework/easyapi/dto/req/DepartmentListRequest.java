package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: DepartmentListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 00:33
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class DepartmentListRequest extends PaginationRequest {

    private String tenantId;
    /**
     * 部门的状态
     * @author pddon.com
     */
    private String depStatus;

    /**
     * 部门上级部门的ID
     * @author pddon.com
     */
    private Long parentId;

    /**
     * 部门内leader的用户ID
     * @author pddon.com
     */
    private String leaderId;
    /**
     * 部门所属分类
     * @author pddon.com
     */
    private String typeId;

    private String keyword;
}
