package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.DictService;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.response.ItemResponse;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.dto.request.DictGroupListRequest;
import com.pddon.framework.easyapi.dao.dto.request.DictListRequest;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.properties.EasyApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName: PublicResController
 * @Description: 公共资源配置查询
 * @Author: Allen
 * @Date: 2024-05-10 01:03
 * @Addr: https://pddon.cn
 */
@RestController
@RequestMapping("public/res")
@IgnoreTenant
public class PublicResController {

    @Autowired
    private DictService dictService;
    @Autowired
    @Lazy
    private EasyApiConfig easyApiConfig;

    @GetMapping("methodCache")
    @RequiredSession
    public ItemResponse<Boolean> methodCache(@RequestParam(value = "switchFlag", required = false) Boolean switchFlag){
        if(switchFlag != null && (switchFlag == true)){
            easyApiConfig.setDisableMethodCache(!easyApiConfig.getDisableMethodCache());
        }
        return new ItemResponse<>(easyApiConfig.getDisableMethodCache());
    }

    @PostMapping("listDict")
    @RequiredSign(scope = SignScope.REQUEST)
    @CacheMethodResult(expireSeconds = 180, expireMode = CacheExpireMode.EXPIRE_AFTER_WRITE)
    public PaginationResponse<DictItem> listDict(@RequestBody DictListRequest req){
        return dictService.list(req);
    }

    @GetMapping("getByGroup")
    @RequiredSign(scope = SignScope.REQUEST)
    @CacheMethodResult(expireSeconds = 180)
    public List<DictItem> getByGroup(@RequestParam(value = "tenantId", required = false) String tenantId, @RequestParam(value = "dictAppId", required = false) String dictAppId, @RequestParam("groupId") String groupId){
        return dictService.getTenantDictsByGroupId(tenantId, dictAppId, groupId);
    }

    @PostMapping("listGroup")
    @RequiredSign(scope = SignScope.REQUEST)
    @CacheMethodResult(expireSeconds = 180, expireMode = CacheExpireMode.EXPIRE_AFTER_WRITE)
    public PaginationResponse<DictGroup> listGroup(@RequestBody DictGroupListRequest req){
        return dictService.listGroup(req);
    }
}
