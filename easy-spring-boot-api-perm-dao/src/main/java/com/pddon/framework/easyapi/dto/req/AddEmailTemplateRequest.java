package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @ClassName: AddEmailTemplateRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 23:28
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddEmailTemplateRequest implements Serializable {
    /**
     * 模板所属租户ID
     * @author pddon.com
     */
    private String tenantId;
    /**
     * 模板所属应用ID
     * @author pddon.com
     */
    private String itemAppId;
    /**
     * 邮件应用场景，字典分组sceneId下的子分组列表，可以通过子字典分组新增业务应用场景
     * @author pddon.com
     */
    @NotEmpty
    private String sceneId;
    /**
     * 邮件应用场景下特定的资源专属使用，通过新增sceneId的子分组下的字典，新增资源ID
     * @author pddon.com
     */
    private String resourceId;
    /**
     * 模板标题
     * @author pddon.com
     */
    @NotEmpty
    private String title;
    /**
     * 内容类型 取值：HTML html代码、MARKDOWN markdown格式文本、 TEXT 纯文本
     * @author pddon.com
     */
    @NotEmpty
    private String contentType;
    /**
     * 模板内容
     * @author pddon.com
     */
    @NotEmpty
    private String content;
    /**
     * 是否启用模板
     * @author pddon.com
     */
    private Boolean enabled;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String comments;
}
