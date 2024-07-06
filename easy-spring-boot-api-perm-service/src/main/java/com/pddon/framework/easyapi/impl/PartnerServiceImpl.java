package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.PartnerService;
import com.pddon.framework.easyapi.annotation.LockDistributed;
import com.pddon.framework.easyapi.consts.PartnerStatus;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.BaseApplicationConfigDao;
import com.pddon.framework.easyapi.dao.PartnerItemDao;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.dto.request.UpdateItemFlagRequest;
import com.pddon.framework.easyapi.dao.entity.BaseApplicationConfig;
import com.pddon.framework.easyapi.dao.entity.PartnerItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.RandomTokenGenerator;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * @ClassName: PartnerServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 23:40
 * @Addr: https://pddon.cn
 */
@Service
@IgnoreTenant
@Slf4j
public class PartnerServiceImpl implements PartnerService {

    private static final String HOST_TENANT_ID = "EASY_API_HOST_TENANT";

    @Autowired
    private PartnerItemDao partnerItemDao;

    @Autowired
    private BaseApplicationConfigDao baseApplicationConfigDao;

    private String generateTenantId(){
        String tenantId = RandomTokenGenerator.generateToken(8);
        if(partnerItemDao.existsTenantId(tenantId)){
            return generateTenantId();
        }
        return tenantId;
    }

    private String generateAppId(){
        String appId = RandomTokenGenerator.generateToken(10);
        if(baseApplicationConfigDao.existsAppId(appId)){
            return generateTenantId();
        }
        return appId;
    }

    @Override
    @LockDistributed
    @Transactional
    public void checkAndCreateHostPartner(){
        if(partnerItemDao.existsTenantId(HOST_TENANT_ID)){
            return;
        }
        PartnerItem item = new PartnerItem();
        item.setPartnerName("默认商户");
        item.setPartnerType("HOST");
        item.setSecret("1KAMHJ41ENMNMRQL");
        item.setPartnerStatus(PartnerStatus.ACTIVE.name());
        item.setTenantId(HOST_TENANT_ID);
        item.setCompanyName("公司名（待修改）");
        item.setEmail("联系邮箱(待修改)");
        item.setUsername("联系人（待修改）");
        item.setEnableApi(true);
        item.setCountryName("中国");
        partnerItemDao.saveItem(item);
    }

    @Override
    public IdResponse addPartner(AddPartnerRequest req) {
        PartnerItem item = new PartnerItem();
        BeanUtils.copyProperties(req, item);
        item.setPartnerStatus(PartnerStatus.TEST.name());
        item.setTenantId(generateTenantId());
        partnerItemDao.saveItem(item);
        return new IdResponse(item.getId());
    }

    @Override
    public void updatePartner(UpdatePartnerRequest req) {
        PartnerItem item = partnerItemDao.getByItemId(req.getId());
        if(item == null){
            throw new BusinessException("商户不存在!");
        }
        BeanUtils.copyProperties(req, item);
        if(StringUtils.isEmpty(item.getTenantId())){
            item.setTenantId(generateTenantId());
        }
        partnerItemDao.updateByItemId(item);
    }

    @Override
    public void removePartner(IdsRequest req) {
        partnerItemDao.removeByIds(Arrays.asList(req.getIds()));
    }

    @Override
    public PaginationResponse<PartnerItem> listPartner(PartnerListRequest req) {
        IPage<PartnerItem> itemPage = partnerItemDao.pageQuery(req);
        PaginationResponse<PartnerItem> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public IdResponse addApp(AddAppRequest req) {
        BaseApplicationConfig applicationConfig = new BaseApplicationConfig();
        BeanUtils.copyProperties(req, applicationConfig);
        String appId = req.getPartnerAppId();
        if (StringUtils.isEmpty(appId)) {
            appId = generateAppId();
        }
        applicationConfig.setAppId(appId);
        baseApplicationConfigDao.saveItem(applicationConfig);
        return new IdResponse(applicationConfig.getId());
    }

    @Override
    public void updateApp(UpdateAppRequest req) {
        BaseApplicationConfig config = baseApplicationConfigDao.getByItemId(req.getId());
        if(config == null){
            throw new BusinessException("应用信息未找到!");
        }
        BeanUtils.copyProperties(req, config);
        config.setAppId(req.getPartnerAppId());
        baseApplicationConfigDao.updateByItemId(config);
    }

    @Override
    public void deleteApp(IdsRequest req) {
        baseApplicationConfigDao.removeByAppIds(Arrays.asList(req.getIds()));
    }

    @Override
    public PaginationResponse<BaseApplicationConfig> listApp(AppListRequest req) {
        IPage<BaseApplicationConfig> itemPage = baseApplicationConfigDao.pageQuery(req, req.getTenantId(), req.getKeyword());
        PaginationResponse<BaseApplicationConfig> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public void updatePartnerStatus(UpdatePartnerStatusRequest req) {
        PartnerItem item = partnerItemDao.getByItemId(req.getId());
        if(item == null){
            throw new BusinessException("商户不存在!");
        }
        partnerItemDao.updateStatus(req.getId(), req.getPartnerStatus().name());
    }

    @Override
    public void updateAppStatus(UpdateItemFlagRequest req) {
        BaseApplicationConfig config = baseApplicationConfigDao.getByItemId(req.getId());
        if(config == null){
            throw new BusinessException("应用信息未找到!");
        }
        config.setEnable(req.getFlag());
        baseApplicationConfigDao.updateByItemId(config);
    }
}
