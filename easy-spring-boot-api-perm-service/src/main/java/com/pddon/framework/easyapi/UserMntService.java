package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.dto.request.UpdateUserPassRequest;
import com.pddon.framework.easyapi.dao.dto.request.UserListRequest;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dao.dto.request.AddUserRequest;
import com.pddon.framework.easyapi.dao.dto.request.UpdateUserRequest;
import com.pddon.framework.easyapi.dto.resp.UserInfoDto;

/**
 * @ClassName: UserMntService
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-25 17:25
 * @Addr: https://pddon.cn
 */
public interface UserMntService {
    UserInfoDto getCurrentUserInfo();

    PaginationResponse<BaseUser> list(UserListRequest req);

    void add(AddUserRequest req);

    void update(UpdateUserRequest req);

    void delete(IdsRequest req);

    void updatePass(UpdateUserPassRequest req);

    void resetPass(Long id);
}
