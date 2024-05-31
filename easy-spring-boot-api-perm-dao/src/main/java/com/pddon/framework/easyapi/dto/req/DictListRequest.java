package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: DictListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 22:04
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class DictListRequest extends PaginationRequest {

    private String dictId;
    private String tenantId;
    /**
     * 字典所属应用ID
     * @author pddon.com
     */
    private String dictAppId;
    /**
     * 字典所属用户ID
     * @author pddon.com
     */
    private String dictUserId;
    /**
     * 字典分组ID
     * @author pddon.com
     */
    private String groupId;

    private String keyword;
}
