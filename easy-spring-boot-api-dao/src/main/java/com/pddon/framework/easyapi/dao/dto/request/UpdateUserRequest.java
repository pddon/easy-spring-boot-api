package com.pddon.framework.easyapi.dao.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @ClassName: UpdateUserRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-27 19:04
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class UpdateUserRequest implements Serializable {
    @NotNull
    private Long id;

    private String tenantId;

    /**
     * 用户所属部门ID
     * @author pddon.com
     */
    private Long depId;

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

    private String password;

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
    private String userCountryCode;

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
     * 个人简介
     * @author pddon.com
     */
    private String intro;

    /**
     *
     * @author pddon.com
     */
    private String logoImgKey;

    /**
     * 用户头像url地址
     * @author pddon.com
     */
    private String avatar;

    /**
     * 拥有的角色
     */
    private List<String> roleIds;
    /**
     * 拥有的权限
     */
    private List<String> permIds;
}
