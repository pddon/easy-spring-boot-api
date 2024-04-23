package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户账号登录记录信息
 * @author pddon.com
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("user_login_record")
public class UserLoginRecord extends BaseTenantEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户账号唯一标识
     * @author pddon.com
     */
     private String userId;

    /**
     * 登录会话ID
     * @author pddon.com
     */
    private String sessionId;

    /**
     * 登录设备ID
     */
    private String deviceId;
     
    /**
     * 用户昵称
     * @author pddon.com
     */
     private String username;

    /**
     * 登录类型
     * @author pddon.com
     */
    private String loginType;

    /**
     * 登录时间
     * @author pddon.com
     */
     private Date loginTime;

}