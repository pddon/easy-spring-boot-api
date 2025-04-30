package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.dto.req.dto.AuthPermDto;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: AuthToRoleRequest
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-29 18:16
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AuthToRoleRequest implements Serializable {
    @NotNull
    private List<String> roleIds;
    @NotNull
    private List<AuthPermDto> perms;
    /**
     * 取消授权
     */
    private boolean unAuth;
}
