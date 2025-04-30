package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.dto.req.dto.AuthPermDto;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: AuthToUserRequest
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-29 18:16
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AuthToUserRequest implements Serializable {
    @NotNull
    private List<String> userIds;
    @NotNull
    private List<AuthPermDto> perms;
    /**
     * 取消授权
     */
    private boolean unAuth;
}
