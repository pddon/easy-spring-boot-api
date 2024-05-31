package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.dao.consts.PageContentType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName: SendEmailRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-31 23:40
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class SendEmailRequest implements Serializable {
    @NotEmpty
    private String email;
    private String title;
    @NotEmpty
    private String content;
    @NotNull
    private PageContentType type;
}
