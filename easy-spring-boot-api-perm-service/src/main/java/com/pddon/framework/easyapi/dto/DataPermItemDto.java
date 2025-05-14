package com.pddon.framework.easyapi.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @ClassName: DataPermItemDto
 * @Description:
 * @Author: Allen
 * @Date: 2025-05-13 23:42
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class DataPermItemDto implements Serializable {
    /**
     * 数据范围权限id
     * @author pddon.com
     */
    private String permId;

    /**
     * 拥有的权限值
     * @author pddon.com
     */
    private String permValue;
}
