package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: EmailTemplate
 * @Description: 邮件模板
 * @Author: Allen
 * @Date: 2024-05-29 11:43
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("email_template")
public class EmailTemplate extends BaseTenantEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 模板所属应用ID
     * @author pddon.com
     */
    private String appId;
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
    /**
     * 模板标题
     * @author pddon.com
     */
    private String title;
    /**
     * 内容类型 取值：HTML html代码、MARKDOWN markdown格式文本、 TEXT 纯文本
     * @author pddon.com
     */
    private String contentType;
    /**
     * 模板内容
     * @author pddon.com
     */
    private String content;
    /**
     * 模板设计稿内容
     * @author pddon.com
     */
    private String editableContent;
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
