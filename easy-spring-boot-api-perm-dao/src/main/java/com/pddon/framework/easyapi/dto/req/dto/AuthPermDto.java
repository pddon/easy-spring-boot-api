package com.pddon.framework.easyapi.dto.req.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: AuthPermDto
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-29 18:40
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AuthPermDto implements Serializable {
    @NotEmpty
    private String permId;
    @NotEmpty
    private List<Object> values;
}
