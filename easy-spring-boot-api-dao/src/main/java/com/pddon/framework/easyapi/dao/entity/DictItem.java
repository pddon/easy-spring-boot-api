package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 字典项
 * @author pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("dict_item")
public class DictItem extends BaseTenantEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 字典所属应用ID
     * @author pddon.com
     */
    private String appId;
    /**
     * 字典所属用户ID
     * @author pddon.com
     */
    private String userId;
    /**
     * 字典分组ID
     * @author pddon.com
     */
     private String groupId;
    /**
     * 字典Id
     */
    private String dictId;
    /**
     * 内容值
     * @author pddon.com
     */
     private String content;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String description;

}