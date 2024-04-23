package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.request.SendAuthCodeRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;

public interface EmailService {
    boolean sendAuthCodeEmail(@RequestBody SendAuthCodeRequest req, Long expireSeconds);

    void checkEmailAuthCode(String email, String type, String authCode, Long expireSeconds);

    void checkEmailAuthCode(String email, String authCode, Long expireSeconds);

    /**
     * 发送通知运营人员邮件
     * @param title
     * @param templateFilename
     * @param params
     * @return
     */
    void sendNotifyServiceEmail(String title, String templateFilename, String ...params);

    void sendNotifyServiceEmail(String title, String content);

    /**
     * 发送邮件消息给用户
     * @param to
     * @param title
     * @param content
     */
    @Async
    void sendNotifyEmail(String to, String title, String content);
}
