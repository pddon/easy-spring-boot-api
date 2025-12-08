package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: DataPermissionResource
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:21
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("data_permission_resource")
public class DataPermissionResource extends BaseEntity {
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
     * 资源所属类型，默认为：TABLE 数据库表
     * @author pddon.com
     */
    private String resType;

    /**
     * 例如：数据库表名
     * @author pddon.com
     */
    private String resName;

    /**
     * 例如：数据库字段
     * @author pddon.com
     */
    private String resField;

    /**
     * 是否禁用该资源数据权限控制
     * @author pddon.com
     */
    private Boolean disabled;

    /**
     *
     * @author pddon.com
     */
    private String comment;

}
