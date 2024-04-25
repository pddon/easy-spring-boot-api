package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.dto.resp.UserInfoDto;

import java.util.Set;

/**
 * @ClassName: UserMntService
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-25 17:25
 * @Addr: https://pddon.cn
 */
public interface UserMntService {
    UserInfoDto getCurrentUserInfo();
}
