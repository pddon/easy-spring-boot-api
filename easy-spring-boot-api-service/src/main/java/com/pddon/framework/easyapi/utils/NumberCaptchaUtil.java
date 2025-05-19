package com.pddon.framework.easyapi.utils;

import cn.hutool.captcha.*;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;

/**
 * @ClassName: NumberCaptchaUtil
 * @Description:
 * @Author: Allen
 * @Date: 2025-05-19 23:06
 * @Addr: https://pddon.cn
 */
public class NumberCaptchaUtil {

    public static ICaptcha generate() {
        return generate(null);
    }
    public static ICaptcha generate(Integer codeCount) {
        if(codeCount == null){
            codeCount = 4;
        }
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 35, codeCount, 10);
        captcha.setGenerator(new RandomGenerator(codeCount));
        captcha.createCode();
        return captcha;
    }
}
