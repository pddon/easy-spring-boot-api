package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @ClassName: AddAppRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 21:30
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddAppRequest implements Serializable {

    /**
     * 应用ID
     * @author pddon.com
     */
    private String appId;

    /**
     * 应用归属的商户ID
     */
    private String tenantId;

    /**
     * 应用名
     * @author pddon.com
     */
    @NotEmpty
    private String appName;

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
    @NotEmpty
    private String privateSecret;

    /**
     * 公钥，用于非对称加解密的秘钥对
     * @author pddon.com
     */
    @NotEmpty
    private String publicSecret;

    /**
     * 回调通知地址
     * @author pddon.com
     */
    private String notifyUrl;

    /**
     * 描述信息
     * @author pddon.com
     */
    private String description;

    /**
     * 是否允许访问接口
     * @author pddon.com
     */
    private Boolean enable;

    /**
     * ip访问黑名单，英文逗号分隔
     * @author pddon.com
     */
    private String blackIpList;

    /**
     * 单位时间窗口内，单个用户访问的最大频次数
     * @author pddon.com
     */
    private Long userMaxAccessCount;

    /**
     * 单位时间窗口内，单个用户单个会话期间访问的最大频次数
     * @author pddon.com
     */
    private Long userSessionMaxAccessCount;

    /**
     * 单位时间窗口内，接口总访问的最大频次数
     * @author pddon.com
     */
    private Long totalMaxAccessCount;

    /**
     * 时间窗口，当前时间减去这个值所得到的初始值为统计区间,startTime = endTime - timeSection; endTime = currentTime
     * @author pddon.com
     */
    private Long timeSection;
}
