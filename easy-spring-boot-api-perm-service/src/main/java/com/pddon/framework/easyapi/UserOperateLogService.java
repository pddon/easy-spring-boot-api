package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.UserOperateRecord;
import com.pddon.framework.easyapi.dto.req.OperateLogListRequest;

import java.util.Date;

/**
 * @ClassName: UserOperateLogService
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 14:51
 * @Addr: https://pddon.cn
 */
public interface UserOperateLogService {

    boolean addLog(String operateType, String apiName, Date startTime, Boolean completed, String errorMsg);

    PaginationResponse<UserOperateRecord> list(OperateLogListRequest req);
}
