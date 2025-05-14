package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @ClassName: AddDataPermissionResoruceRequest
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 21:37
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddDataPermissionResoruceRequest implements Serializable {
    /**
     * 数据范围权限id
     * @author pddon.com
     */
    @NotEmpty
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
    @NotEmpty
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
