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
 * 用户账号信息
 * @author pddon.com
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("user")
public class BaseUser extends BaseTenantEntity{
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
     * 用户最近一次登录会话ID
     * @author pddon.com
     */
    private String sessionId;

    /**
     * 最近一次登录设备ID
     */
    private String deviceId;
     
    /**
     * 用户真实姓名
     * @author pddon.com
     */
     private String username;
     
    /**
     * 用户昵称
     * @author pddon.com
     */
     private String nickname;
     
    /**
     * 用户邮箱号
     * @author pddon.com
     */
     private String email;
     
    /**
     * 用户绑定的手机号
     * @author pddon.com
     */
     private String phone;

    /**
     * 职业工作
     * @author pddon.com
     */
    private String job;

    /**
     * 从业行业
     * @author pddon.com
     */
    private String industry;
     
    /**
     * 用户归属地国家编码
     * @author pddon.com
     */
     private String countryCode;
     
    /**
     * 用户头像url地址
     * @author pddon.com
     */
     private String avatar;
     
    /**
     * 用户账号登录密码
     * @author pddon.com
     */
     private String password;
     
    /**
     * 用户归属地国家名
     * @author pddon.com
     */
     private String countryName;
     
    /**
     * 用户性别
     * @author pddon.com
     */
     private Boolean sex;
     
    /**
     * 用户生日
     * @author pddon.com
     */
     private Date birthday;
     
    /**
     * 用户账号状态：ACTIVE 已激活，FROZEN 冻结中
     * @author pddon.com
     */
     private String accountStatus;
     
    /**
     * 用户注册时间
     * @author pddon.com
     */
     private Date regTime;
     
    /**
     * 用户最近一次登录时间
     * @author pddon.com
     */
     private Date lastLoginTime;

    /**
     * 个人简介
     * @author pddon.com
     */
    private String intro;

    /**
     * 最近登录使用的应用ID
     * @author pddon.com
     */
    private String appId;

    /**
     * 用户使用的语言类型
     * @author pddon.com
     */
    private String locale;

    /**
     * 用户使用软件的版本号
     * @author pddon.com
     */
    private String appVersion;

}