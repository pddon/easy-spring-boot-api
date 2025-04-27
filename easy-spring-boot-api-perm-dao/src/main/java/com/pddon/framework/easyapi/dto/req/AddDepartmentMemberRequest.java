package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: AddDepartmentMemberRequest
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 00:50
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddDepartmentMemberRequest implements Serializable {

    @NotNull
    private Long departmentId;
    @NotNull
    private List<String> userIds;
}
