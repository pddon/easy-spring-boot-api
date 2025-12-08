package com.pddon.framework.easyapi.impl;

import cn.hutool.captcha.ICaptcha;
import cn.hutool.captcha.ShearCaptcha;
import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.CaptchaService;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.utils.IOUtils;
import com.pddon.framework.easyapi.utils.NumberCaptchaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName: CaptchaServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2025-05-19 23:16
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {
    @Autowired
    private CacheManager cacheManager;

    @Override
    public void generateCaptcha(String id, HttpServletResponse response) {
        ICaptcha captcha = NumberCaptchaUtil.generate();
        String code = captcha.getCode();
        cacheManager.set(id, code, 300L, CacheExpireMode.EXPIRE_AFTER_WRITE);
        try {
            captcha.write(response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (IOException e) {
            log.warn(IOUtils.getThrowableInfo(e));
        }
    }

    @Override
    public boolean validate(String id, String code) {
        Object number = cacheManager.get(id, 300L, CacheExpireMode.EXPIRE_AFTER_WRITE);
        if(number == null){
            return false;
        }
        boolean re = number.equals(code);
        if(re){
            cacheManager.remove(id, 300L, CacheExpireMode.EXPIRE_AFTER_WRITE);
        }
        return re;
    }
}
