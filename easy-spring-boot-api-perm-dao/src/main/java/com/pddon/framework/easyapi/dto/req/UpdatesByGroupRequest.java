package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.dto.req.dto.DictDto;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: UpdatesByGroupRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-01 00:24
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class UpdatesByGroupRequest implements Serializable {

    @NotEmpty
    private String groupId;
    private String tenantId;
    private String dictAppId;
    private String userId;
    @NotNull
    private List<DictDto> items;
}
