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
 * 用户操作记录信息
 * @author pddon.com
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("user_operate_record")
public class UserOperateRecord extends BaseTenantEntity{
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
     * 用户账号名
     * @author pddon.com
     */
     private String username;

    /**
     * 操作类型
     * @author pddon.com
     */
    private String operateType;

    /**
     * 接口名
     * @author pddon.com
     */
    private String apiName;
    /**
     * 是否操作完成了
     */
    private Boolean completed;
    /**
     * 操作失败原因
     */
    private String errorMsg;
    /**
     * 操作时间
     * @author pddon.com
     */
     private Date operateTime;

    /**
     * 操作时间
     * @author pddon.com
     */
    private Date endTime;

}