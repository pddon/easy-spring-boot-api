package com.pddon.framework.easyapi.config;

import com.pddon.framework.easyapi.DictService;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @ClassName: EmailConfig
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-01 00:44
 * @Addr: https://pddon.cn
 */
@Configuration
@Slf4j
public class EmailConfig {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MailProperties mailProperties;
    @Autowired
    private DictService dictService;

    @PostConstruct
    public void initDbConfig(){
        if(!(mailSender instanceof JavaMailSenderImpl)){
            return;
        }
        List<DictItem> items = dictService.getDefaultDictsByGroupId("emailServerConfigs");
        if(items.isEmpty()){
            return;
        }
        Map<String, String> map = items.stream().collect(Collectors.toMap(DictItem::getDictId, DictItem::getContent, (item1, item2) -> item1));
        JavaMailSenderImpl sender = (JavaMailSenderImpl) mailSender;
        if(map.containsKey("mail.host")){
            sender.setHost(map.get("mail.host"));
            map.remove("mail.host");
        }
        if(map.containsKey("mail.port")){
            sender.setPort(Integer.valueOf(map.get("mail.port")));
            map.remove("mail.port");
        }
        if(map.containsKey("mail.username")){
            sender.setUsername(map.get("mail.username"));
            map.remove("mail.username");
        }
        if(map.containsKey("mail.password")){
            sender.setPassword(map.get("mail.password"));
            map.remove("mail.password");
        }
        if(map.containsKey("mail.protocol")){
            sender.setProtocol(map.get("mail.protocol"));
            map.remove("mail.protocol");
        }
        Properties properties = new Properties();
        properties.putAll(mailProperties.getProperties());
        properties.putAll(map);
        sender.setJavaMailProperties(properties);
        if(log.isTraceEnabled()){
            log.trace("init db mail config success.");
        }
    }
}
