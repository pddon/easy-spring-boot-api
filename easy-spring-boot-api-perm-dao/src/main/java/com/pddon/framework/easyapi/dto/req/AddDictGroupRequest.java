package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @ClassName: AddDictGroupRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 01:14
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddDictGroupRequest implements Serializable {
    /**
     * 字典分组ID
     * @author pddon.com
     */
    @NotEmpty
    private String groupId;
    /**
     * 父分组Id
     */
    private String parentGroupId;
    /**
     * 分组自身内容
     * @author pddon.com
     */
    private String content;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String description;
}
