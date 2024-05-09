package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @ClassName: AddDictRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 01:14
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddDictRequest implements Serializable {
    /**
     * 应用所属渠道ID(租户ID)
     * @author pddon.com
     */
    private String tenantId;
    /**
     * 字典所属应用ID
     * @author pddon.com
     */
    private String appId;
    /**
     * 字典所属用户ID
     * @author pddon.com
     */
    private String userId;
    /**
     * 字典分组ID
     * @author pddon.com
     */
    private String groupId;
    /**
     * 字典Id
     */
    @NotEmpty
    private String dictId;
    /**
     * 内容值
     * @author pddon.com
     */
    @NotEmpty
    private String content;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String description;
}
