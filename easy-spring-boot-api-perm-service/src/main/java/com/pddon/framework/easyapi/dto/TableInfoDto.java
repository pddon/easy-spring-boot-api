package com.pddon.framework.easyapi.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: TableInfoDto
 * @Description:
 * @Author: Allen
 * @Date: 2025-05-13 17:26
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class TableInfoDto implements Serializable {
    private String tableName;
    private List<String> fields;
}
