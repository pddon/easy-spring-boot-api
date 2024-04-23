package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.consts.EmailTemplateType;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.controller.request.SendAuthCodeRequest;
import com.pddon.framework.easyapi.EmailService;
import com.pddon.framework.easyapi.email.dto.EmailAuthTokenDto;
import com.pddon.framework.easyapi.email.dto.EmailTemplateConfig;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.EmailUtil;
import com.pddon.framework.easyapi.utils.FileUtil;
import com.pddon.framework.easyapi.utils.IOUtils;
import com.pddon.framework.easyapi.utils.RandomTokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final String KEY_FORMAT = "E:%s:%s:%s";

    @Autowired
    @Lazy
    private CacheManager cacheManager;

    @Value("#{'${easyapi.email.service.notify.users:}'.split(',')}")
    private String[] serviceUserList;

    public static Map<String, EmailTemplateConfig> templates = new HashMap<>();

    static {
        templates.put(EmailTemplateType.AUTH_CODE.name(), new EmailTemplateConfig(EmailTemplateType.AUTH_CODE,
                "sendEmailAuthCode.html", "email.sendAuthCode.register.title",
                "email.sendAuthCode.prefix", "token", "email.sendAuthCode.suffix"));
        templates.put(EmailTemplateType.COMMON_NOTIFY.name(), new EmailTemplateConfig(EmailTemplateType.COMMON_NOTIFY,
                "sendCommonNotify.html", "email.sendCommonNotify.title"));
    }

    @Override
    public boolean sendAuthCodeEmail(SendAuthCodeRequest req, Long expireSeconds) {
        EmailTemplateConfig config = templates.get(EmailTemplateType.AUTH_CODE.name());
        String key = String.format(KEY_FORMAT, EmailTemplateType.AUTH_CODE.name(), req.getEmail(), req.getType());
        String token = RandomTokenGenerator.generateToken();
        String locale = RequestContext.getContext().getLocale();
        //发送邮件验证码
        boolean result = EmailUtil.sendHtmlEmail(req.getEmail(),
                config.getTitle(),
                config.getPath(), locale,
                config.getParams());
        if(result){
            //缓存验证码
            EmailAuthTokenDto tokenDto = new EmailAuthTokenDto();
            tokenDto.setToken(token)
                    .setCrtTime(System.currentTimeMillis());
            cacheManager.set(key, tokenDto, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
            return true;
        }
        return false;
    }

    @Override
    public void checkEmailAuthCode(String email, String authCode, Long expireSeconds) {
        this.checkEmailAuthCode(email, "C", authCode, expireSeconds);
    }

    @Override
    public void checkEmailAuthCode(String email, String type, String authCode, Long expireSeconds) {
        String key = String.format(KEY_FORMAT, EmailTemplateType.AUTH_CODE.name(), email, type);
        EmailAuthTokenDto tokenDto = cacheManager.get(key, EmailAuthTokenDto.class, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
        if(tokenDto == null){
            throw new BusinessException("AUTH_CODE_EXPIRED");
        }
        if(!tokenDto.getToken().equals(authCode)){
            throw new BusinessException("AUTH_CODE_ERROR");
        }else{
            //删除验证码
            cacheManager.remove(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
        }
    }

    @Async
    @Override
    public void sendNotifyServiceEmail(String title, String templateFilename, String ...params) {
        try {
            String content = MessageFormat.format(FileUtil.loadClassPathFileAsString(templateFilename),
                    params);
            this.sendNotifyServiceEmail(title, content);
        } catch (IOException e) {
            log.warn("HTML邮件失败，模板未找到!");
            log.warn(IOUtils.getThrowableInfo(e));
        }
    }

    @Async
    @Override
    public void sendNotifyServiceEmail(String title, String content) {
        Arrays.stream(this.serviceUserList).forEach(to -> {
            EmailUtil.sendHtmlEmail(to, title, content);
        });
    }

    @Async
    @Override
    public void sendNotifyEmail(String to, String title, String content){
        EmailTemplateConfig config = templates.get(EmailTemplateType.AUTH_CODE.name());
        try {
            String emailContent = MessageFormat.format(FileUtil.loadClassPathFileAsString(config.getPath()),
                    Arrays.asList(title, content));
            EmailUtil.sendHtmlEmail(to, config.getTitle(), emailContent);
        } catch (IOException e) {
            log.warn("HTML邮件失败，模板未找到!");
            log.warn(IOUtils.getThrowableInfo(e));
        }
    }
}
