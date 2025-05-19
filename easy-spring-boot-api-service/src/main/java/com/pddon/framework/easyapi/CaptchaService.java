package com.pddon.framework.easyapi;

import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName: CaptchaService
 * @Description:
 * @Author: Allen
 * @Date: 2025-05-19 23:13
 * @Addr: https://pddon.cn
 */
public interface CaptchaService {

    void generateCaptcha(String id, HttpServletResponse response);

    boolean validate(String id, String code);
}
