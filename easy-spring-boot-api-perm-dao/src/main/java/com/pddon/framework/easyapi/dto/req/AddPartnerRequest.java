package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName: AddPartnerRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 21:30
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddPartnerRequest implements Serializable {

    /**
     * 业务应用名
     * @author pddon.com
     */
    @NotEmpty
    private String partnerName;

    /**
     * 商户的类型
     * @author pddon.com
     */
    @NotEmpty
    private String partnerType;

    /**
     * 用于对称加解密、生成数字签名、验证数字签名的秘钥
     * @author pddon.com
     */
    @NotEmpty
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
    @NotEmpty
    private String companyName;

    /**
     * 公司Logo图标ID
     * @author pddon.com
     */
    private String logoKey;

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
    @NotEmpty
    private String username;

    /**
     * 联系人的邮箱号
     * @author pddon.com
     */
    @NotEmpty
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
    @NotNull
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
