package com.pddon.framework.easyapi.dao.dto.req;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @ClassName: EmailRecordListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-01 11:14
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class EmailRecordListRequest extends PaginationRequest {
    /**
     * 应用所属渠道ID(租户ID)
     * @author pddon.com
     */
    private String tenantId;
    /**
     * 是否发送成功模板
     * @author pddon.com
     */
    private Boolean success;
    /**
     * 用户ID
     * @author pddon.com
     */
    private String toUserId;
    /**
     * 用户ID
     * @author pddon.com
     */
    private String email;
    /**
     * 邮件应用场景，字典分组emailSceneId下的子分组列表，可以通过子字典分组新增业务应用场景
     * @author pddon.com
     */
    private String sceneId;
    /**
     * 邮件应用场景下特定的资源专属使用，通过新增emailSceneId的子分组下的字典，新增资源ID
     * @author pddon.com
     */
    private String resourceId;
    private String keyword;

    private Date startTime;

    private Date endTime;
}
