package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: Department
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-27 21:39
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("department")
public class Department extends BaseTenantEntity {
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 组织机构下部门的名字
     * @author pddon.com
     */
    private String depName;

    /**
     * 部门的状态
     * @author pddon.com
     */
    private String depStatus;

    /**
     * 部门上级部门的ID
     * @author pddon.com
     */
    private Long parentId;

    /**
     * 部门内leader的用户ID
     * @author pddon.com
     */
    private String leaderId;

    /**
     *
     * @author pddon.com
     */
    private String leaderName;

    /**
     * 部门所属分类
     * @author pddon.com
     */
    private String typeId;

    /**
     * 部门所属分类名
     * @author pddon.com
     */
    private String typeName;

    /**
     * 排序值
     * @author pddon.com
     */
    private Long orderValue;

    /**
     * 其他描述信息
     * @author pddon.com
     */
    private String comment;
}
