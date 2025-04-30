package com.pddon.framework.easyapi.dao.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: DataPermDto
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-30 23:21
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class DataPermDto implements Serializable {
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

    private List<String> values;
}
