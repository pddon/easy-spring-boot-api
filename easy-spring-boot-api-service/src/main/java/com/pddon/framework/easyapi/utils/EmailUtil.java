package com.pddon.framework.easyapi.utils;

import com.pddon.framework.easyapi.LanguageTranslateManager;
import com.pddon.framework.easyapi.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EmailUtil {

    private static JavaMailSender mailSender;

    private static MailProperties mailProperties;

    private static LanguageTranslateManager languageTranslateManager;

    @Autowired
    @Lazy
    public void setMailSender(JavaMailSender mailSender) {
        EmailUtil.mailSender = mailSender;
    }

    @Autowired
    @Lazy
    public void setMailProperties(MailProperties mailProperties) {
        EmailUtil.mailProperties = mailProperties;
    }

    @Autowired
    @Lazy
    public void setLanguageTranslateManager(LanguageTranslateManager languageTranslateManager) {
        EmailUtil.languageTranslateManager = languageTranslateManager;
    }

    public static boolean sendHtmlEmail(String toUserAccount, String title, String templatePath, String locale, String ...params){
        try {
            //创建一个MINE消息
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper minehelper = new MimeMessageHelper(message, true);
            //谁发
            minehelper.setFrom(mailProperties.getUsername(), mailProperties.getProperties().get("nickname"));
            //谁要接收
            minehelper.setTo(toUserAccount);
            //邮件主题
            minehelper.setSubject(languageTranslateManager.get(title, locale));
            List<String> localeParams = Arrays.stream(params).map(param -> languageTranslateManager.get(param, locale)).collect(Collectors.toList());
            //邮件内容   true 表示带有附件或html
            minehelper.setText(MessageFormat.format(FileUtil.loadClassPathFileAsString(templatePath),
                    localeParams.toArray()), true);
            mailSender.send(message);
            log.info("HTML邮件发送成功!");
            return true;
        } catch (MailException | MessagingException | IOException e) {
            log.warn(IOUtils.getThrowableInfo(e));
            log.warn("HTML邮件失败");
            return false;
        }
    }

    public static boolean sendEmail(String toUserAccount, String title, String content, boolean isHtml){
        try {
            //创建一个MINE消息
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper minehelper = new MimeMessageHelper(message, true);
            //谁发
            minehelper.setFrom(mailProperties.getUsername(), mailProperties.getProperties().get("nickname"));
            //谁要接收
            minehelper.setTo(toUserAccount);
            //邮件主题
            minehelper.setSubject(languageTranslateManager.get(title, RequestContext.getContext().getLocale()));
            //邮件内容   true 表示带有附件或html
            minehelper.setText(content, isHtml);
            mailSender.send(message);
            log.info("HTML邮件发送成功!");
            return true;
        } catch (MailException | MessagingException | IOException e) {
            log.warn(IOUtils.getThrowableInfo(e));
            log.warn("HTML邮件失败");
            return false;
        }
    }
    public static boolean sendHtmlEmail(String toUserAccount, String title, String content){
        return sendEmail(toUserAccount, title, content, true);
    }
}
