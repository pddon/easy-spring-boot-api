package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: DataPermission
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:21
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("data_permission")
public class DataPermission extends BaseEntity {
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据范围权限id
     * @author pddon.com
     */
    private String permId;

    /**
     * 数据权限的名字
     * @author pddon.com
     */
    private String permName;

    /**
     * 取值： FIELD字段查询； TABLE_FIELD 表字段查询
     * @author pddon.com
     */
    private String queryType;

    /**
     * 查询表名
     * @author pddon.com
     */
    private String queryTable;

    /**
     * 查询字段
     * @author pddon.com
     */
    private String queryField;

    /**
     *
     * @author pddon.com
     */
    private String realField;

    /**
     * 需要转换到该权限进行限制
     * @author pddon.com
     */
    private String realPermId;

    /**
     * 是否禁用该权限
     * @author pddon.com
     */
    private Boolean disabled;

    /**
     *
     * @author pddon.com
     */
    private String comment;

}
