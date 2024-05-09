package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 字典分组
 * @author pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("dict_group")
public class DictGroup extends BaseEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 字典分组ID
     * @author pddon.com
     */
     private String groupId;
    /**
     * 父分组Id
     */
    private String parentGroupId;
    /**
     * 分组自身内容
     * @author pddon.com
     */
     private String content;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String description;

}