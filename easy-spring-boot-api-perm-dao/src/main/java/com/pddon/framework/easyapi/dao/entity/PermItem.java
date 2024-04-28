package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 权限信息
 * @author pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("perm_item")
public class PermItem extends BaseEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 权限ID
     * @author pddon.com
     */
     private String permId;
    /**
     * 父权限Id
     */
    private String parentPermId;
    /**
     * 权限名
     * @author pddon.com
     */
    private String permName;
    /**
     * 资源类型
     * @author pddon.com
     */
     private String resourceType;
    /**
     * 权限简介
     * @author pddon.com
     */
    private String intro;

}