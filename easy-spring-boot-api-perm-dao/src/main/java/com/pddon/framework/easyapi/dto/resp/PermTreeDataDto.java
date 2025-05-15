package com.pddon.framework.easyapi.dto.resp;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: PermTreeDataDto
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-28 23:22
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class PermTreeDataDto implements Serializable {

    private String label;

    private String value;

    private boolean disabled;

    private List<PermTreeDataDto> children;
}
