package com.pddon.framework.easyapi.dto.req.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName: DictDto
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-01 00:26
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class DictDto implements Serializable {
    private Long id;
    @NotEmpty
    private String dictId;
    /**
     * 内容值
     * @author pddon.com
     */
    private String content;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String description;
}
