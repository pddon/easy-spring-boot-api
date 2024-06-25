package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName: UpdateFileRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-25 22:08
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class UpdateFileRequest implements Serializable {
    @NotNull
    private Long id;
    /**
     * 文件名
     * @author pddon.com
     */
    private String filename;
    /**
     * 是否禁用
     * @author pddon.com
     */
    private Boolean disabled;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String comments;
}
