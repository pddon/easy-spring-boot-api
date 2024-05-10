package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName: UpdateDictRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 01:14
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class UpdateDictRequest implements Serializable {
    @NotNull
    private Long id;
    /**
     * 字典Id
     */
    private String dictId;
    /**
     * 内容值
     * @author pddon.com
     */
    private String content;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String description;
    /**
     * 应用所属渠道ID(租户ID)
     * @author pddon.com
     */
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
}
