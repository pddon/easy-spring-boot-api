package com.pddon.framework.easyapi.dao.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName: UpdateItemFlagRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-28 16:13
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class UpdateItemFlagRequest implements Serializable {
    @NotNull
    private Long id;
    @NotNull
    private Boolean flag;
}
