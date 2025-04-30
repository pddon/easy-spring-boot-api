package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @ClassName: AddDataPermissionRequest
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 21:37
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddDataPermissionRequest implements Serializable {
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
     * 是否禁用该权限
     * @author pddon.com
     */
    private Boolean disable;

    /**
     *
     * @author pddon.com
     */
    private String comment;
}
