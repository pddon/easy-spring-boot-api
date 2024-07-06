package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.annotation.LockDistributed;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.dto.request.UpdateItemFlagRequest;
import com.pddon.framework.easyapi.dao.entity.BaseApplicationConfig;
import com.pddon.framework.easyapi.dao.entity.PartnerItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName: PartnerService
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 23:16
 * @Addr: https://pddon.cn
 */
public interface PartnerService {
    @LockDistributed
    @Transactional
    void checkAndCreateHostPartner();

    IdResponse addPartner(AddPartnerRequest req);

    void updatePartner(UpdatePartnerRequest req);

    void removePartner(IdsRequest req);

    PaginationResponse<PartnerItem> listPartner(PartnerListRequest req);

    IdResponse addApp(AddAppRequest req);

    void updateApp(UpdateAppRequest req);

    void deleteApp(IdsRequest req);

    PaginationResponse<BaseApplicationConfig> listApp(AppListRequest req);

    void updatePartnerStatus(UpdatePartnerStatusRequest req);

    void updateAppStatus(UpdateItemFlagRequest req);
}
