package com.pddon.framework.easyapi.dto.resp;

import com.pddon.framework.easyapi.controller.response.SuccessResponse;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @ClassName: IdResponse
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 21:42
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString(callSuper=true)
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
public class IdResponse extends SuccessResponse {

    private Long id;
}
