package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.PartnerService;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.dto.request.UpdateItemFlagRequest;
import com.pddon.framework.easyapi.dao.entity.BaseApplicationConfig;
import com.pddon.framework.easyapi.dao.entity.PartnerItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: PartnerMntController
 * @Description: 商户、应用管理相关接口
 * @Author: Allen
 * @Date: 2024-04-23 23:15
 * @Addr: https://pddon.cn
 */
@Api(tags = "商户相关信息管理接口")
@RestController
@RequestMapping("partner")
public class PartnerMntController {

    @Autowired
    private PartnerService partnerService;

    @PostMapping("add")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增商户", apiName = "partner/add")
    @RequiresPermissions("partner:add")
    public IdResponse addPartner(@RequestBody AddPartnerRequest req){
        return partnerService.addPartner(req);
    }

    @PostMapping("update")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改商户", apiName = "partner/update")
    @RequiresPermissions("partner:update")
    public void updatePartner(@RequestBody UpdatePartnerRequest req){
        partnerService.updatePartner(req);
    }

    @PostMapping("updateStatus")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改商户状态", apiName = "partner/updateStatus")
    @RequiresPermissions("partner:update")
    public void updatePartnerStatus(@RequestBody UpdatePartnerStatusRequest req){
        partnerService.updatePartnerStatus(req);
    }

    @PostMapping("delete")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除商户", apiName = "partner/delete")
    @RequiresPermissions("partner:delete")
    public void removePartner(@RequestBody IdsRequest req){
        partnerService.removePartner(req);
    }

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    //@OperateLog(type="商户列表", apiName = "partner/list")
    @RequiresPermissions("partner:query")
    public PaginationResponse<PartnerItem> listPartner(@RequestBody PartnerListRequest req){
        return partnerService.listPartner(req);
    }

    @PostMapping("addApp")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增应用", apiName = "partner/addApp")
    @RequiresPermissions("application:add")
    public IdResponse addApp(@RequestBody AddAppRequest req){
        return partnerService.addApp(req);
    }

    @PostMapping("updateApp")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改应用", apiName = "partner/updateApp")
    @RequiresPermissions("application:update")
    public void updateApp(@RequestBody UpdateAppRequest req){
        partnerService.updateApp(req);
    }

    @PostMapping("updateAppStatus")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改应用状态", apiName = "partner/updateAppStatus")
    @RequiresPermissions("application:update")
    public void updateAppStatus(@RequestBody UpdateItemFlagRequest req){
        partnerService.updateAppStatus(req);
    }
    @PostMapping("deleteApp")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除应用", apiName = "partner/deleteApp")
    @RequiresPermissions("application:delete")
    public void deleteApp(@RequestBody IdsRequest req){
        partnerService.deleteApp(req);
    }

    @PostMapping("listApp")
    @RequiredSign(scope = SignScope.REQUEST)
    //@OperateLog(type="应用列表", apiName = "partner/listApp")
    @RequiresPermissions("application:query")
    public PaginationResponse<BaseApplicationConfig> listApp(@RequestBody AppListRequest req){
        return partnerService.listApp(req);
    }
}
