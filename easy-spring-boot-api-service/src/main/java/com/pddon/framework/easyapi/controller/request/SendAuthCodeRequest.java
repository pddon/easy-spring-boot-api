package com.pddon.framework.easyapi.controller.request;

import com.pddon.framework.easyapi.annotation.Decrypt;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
public class SendAuthCodeRequest implements Serializable {
    /**
     * 验证码类型
     */
    private String type = "C";
    @Decrypt
    @NotEmpty
    private String email;

    private String title;

    private String content;
}
