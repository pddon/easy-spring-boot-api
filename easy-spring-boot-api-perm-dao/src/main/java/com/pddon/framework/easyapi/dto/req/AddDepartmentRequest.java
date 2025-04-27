package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @ClassName: AddDepartmentRequest
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 00:30
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddDepartmentRequest implements Serializable {
    /**
     * 组织机构下部门的名字
     * @author pddon.com
     */
    @NotEmpty
    private String depName;

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
     * 其他描述信息
     * @author pddon.com
     */
    private String comment;
}
