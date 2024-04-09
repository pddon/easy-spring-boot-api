package com.pddon.framework.easyapi.email.dto;

import com.pddon.framework.easyapi.consts.EmailTemplateType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class EmailTemplateConfig implements Serializable {
    private EmailTemplateType type;

    private String path;

    private String title;

    private String[] params;
    public EmailTemplateConfig(EmailTemplateType type, String path){
        this.type = type;
        this.path = path;
        this.params = new String[8];
    }

    public EmailTemplateConfig(EmailTemplateType type, String path, String title, String... params){
        this.type = type;
        this.path = path;
        this.title = title;
        this.params = params;
    }
}
