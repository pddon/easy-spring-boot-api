package com.pddon.framework.easyapi.dao.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * @ClassName: UpdateUserPassRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-27 19:04
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class UpdateUserPassRequest implements Serializable {
    @NotEmpty
    private String password;
    @NotEmpty
    private String newPassword;
}
