package com.pddon.framework.easyapi.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * @ClassName: UserInfoDto
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-25 21:20
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class UserInfoDto implements Serializable {

    private Long id;
    /**
     * 用户最近一次登录会话ID
     * @author pddon.com
     */
    private String sessionId;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
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
     * 拥有的角色
     */
    private Set<String> roleIds;
    /**
     * 拥有的权限
     */
    private Set<String> perms;
}
