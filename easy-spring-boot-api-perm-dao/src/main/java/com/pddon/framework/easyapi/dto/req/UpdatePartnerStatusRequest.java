package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.consts.PartnerStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName: UpdatePartnerRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 21:30
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class UpdatePartnerStatusRequest implements Serializable {
    @NotNull
    private Long id;

    /**
     * 商户账号状态，用于控制商户对api的调用和后台访问等等
     * @author pddon.com
     */
    @NotNull
    private PartnerStatus partnerStatus;
}
