package com.pddon.framework.easyapi;

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
}
