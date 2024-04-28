package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: PartnerItem
 * @Description: 商户信息
 * @Author: Allen
 * @Date: 2024-04-23 18:09
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("partner_item")
public class PartnerItem extends BaseTenantEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户名
     * @author pddon.com
     */
    private String partnerName;

    /**
     * 商户账号状态，用于控制商户对api的调用和后台访问等等
     * @author pddon.com
     */
    private String partnerStatus;

    /**
     * 商户的类型
     * @author pddon.com
     */
    private String partnerType;

    /**
     * 用于对称加解密、生成数字签名、验证数字签名的秘钥
     * @author pddon.com
     */
    private String secret;

    /**
     * 私钥，用于非对称加解密的秘钥对
     * @author pddon.com
     */
    private String privateSecret;

    /**
     * 公钥，用于非对称加解密的秘钥对
     * @author pddon.com
     */
    private String publicSecret;

    /**
     * 商户所属公司名
     * @author pddon.com
     */
    private String companyName;

    /**
     * 商户公司的logo图片地址
     * @author pddon.com
     */
    private String companyLogo;

    /**
     * 简单备注信息
     * @author pddon.com
     */
    private String description;

    /**
     * 联系人姓名
     * @author pddon.com
     */
    private String username;

    /**
     * 联系人的邮箱号
     * @author pddon.com
     */
    private String email;

    /**
     * 联系人的手机号
     * @author pddon.com
     */
    private String phone;

    /**
     * 是否已启用API调用权限
     * @author pddon.com
     */
    private Boolean enableApi;

    /**
     * 合作方公司归属国家信息
     * @author pddon.com
     */
    private String countryName;

    /**
     * 商户公司默认地址
     * @author pddon.com
     */
    private String address;
}
