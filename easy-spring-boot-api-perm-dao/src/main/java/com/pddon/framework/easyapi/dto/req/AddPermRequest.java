package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: AddPermRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 21:30
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddPermRequest implements Serializable {

    /**
     * 权限ID
     * @author pddon.com
     */
    private String permId;
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
