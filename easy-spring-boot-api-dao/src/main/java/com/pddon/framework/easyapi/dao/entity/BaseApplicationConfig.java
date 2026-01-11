package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 业务应用配置信息
 * @author pddon.com
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("application_config")
public class BaseApplicationConfig extends BaseTenantEntity {

    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 应用ID
     * @author pddon.com
     */
    private String appId;

    /**
     * 应用名
     * @author pddon.com
     */
    private String appName;
    /**
     * 应用类型 COMMON 通用应用 PAY 支付应用 FOOD 餐饮应用 TOOL 工具应用
     * @author pddon.com
     */
    private String appType;
    /**
     * 各类型应用的专用配置
     * @author pddon.com
     */
    private String typeConfig;

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

    /**
     * 应用logo图标地址
     * @author pddon.com
     */
    private String logoUrl;

    /**
     * logo图标的ID
     * @author pddon.com
     */
    private String logoKey;
}
