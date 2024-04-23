package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.BaseApplicationConfig;
import com.pddon.framework.easyapi.dao.entity.PartnerItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;

/**
 * @ClassName: PartnerService
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 23:16
 * @Addr: https://pddon.cn
 */
public interface PartnerService {
    IdResponse addPartner(AddPartnerRequest req);

    void updatePartner(UpdatePartnerRequest req);

    void removePartner(IdsRequest req);

    PaginationResponse<PartnerItem> listPartner(PartnerListRequest req);

    IdResponse addApp(AddAppRequest req);

    void updateApp(UpdateAppRequest req);

    void deleteApp(IdsRequest req);

    PaginationResponse<BaseApplicationConfig> listApp(AppListRequest req);
}
