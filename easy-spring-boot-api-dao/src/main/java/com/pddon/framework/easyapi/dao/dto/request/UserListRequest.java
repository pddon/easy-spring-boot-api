package com.pddon.framework.easyapi.dao.dto.request;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import com.pddon.framework.easyapi.dao.consts.UserKeyType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @ClassName: UserListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 22:04
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class UserListRequest extends PaginationRequest {
    /**
     * 应用所属渠道ID(租户ID)
     * @author pddon.com
     */
    private String tenantId;
    private String accountStatus;
    private UserKeyType type;
    /**
     * 部门ID
     */
    private Long depId;
    private String keyword;
    private Date startTime;

    private Date endTime;
}
