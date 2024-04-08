package com.pddon.framework.easyapi.component;

import com.pddon.framework.easyapi.dto.ErrorCodeDto;

import java.util.List;

/**
 * @ClassName: BusinessErrorCodeInterpreter
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-08 20:51
 * @Addr: https://pddon.cn
 */
public interface BusinessErrorCodeInterpreter {
    Integer getCode(String error);

    List<ErrorCodeDto> getBussinessCodes();

    List<ErrorCodeDto> getSystemCodes();
}
