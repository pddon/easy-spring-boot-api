package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: EmailSendRecord
 * @Description: 邮件发送记录
 * @Author: Allen
 * @Date: 2024-07-01 10:23
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("email_send_record")
public class EmailSendRecord extends BaseTenantEntity {
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 接收者用户ID
     * @author pddon.com
     */
    private String userId;
    /**
     * 收件箱地址
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
    /**
     * 模板标题
     * @author pddon.com
     */
    private String title;
    /**
     * 模板内容
     * @author pddon.com
     */
    private String content;
    /**
     * 是否发送成功模板
     * @author pddon.com
     */
    private Boolean success;
    /**
     * 失败原因
     * @author pddon.com
     */
    private String errMsg;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String comments;
}
