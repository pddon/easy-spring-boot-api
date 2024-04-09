package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.ApplicationManager;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.consts.CacheKeyMode;
import com.pddon.framework.easyapi.dao.BaseApplicationConfigDao;
import com.pddon.framework.easyapi.dao.entity.BaseApplicationConfig;
import com.pddon.framework.easyapi.dto.ApiPermissionDto;
import com.pddon.framework.easyapi.dto.SecretKeyPair;
import com.pddon.framework.easyapi.properties.ChannelConfigProperties;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName: DbAppConfigManager
 * @Description: 应用接口调用实现
 * @Author: Allen
 * @Date: 2024-03-05 20:52
 * @Addr: https://pddon.cn
 */
@Component
@Slf4j
@NoArgsConstructor
public class DbAppConfigManager extends DefaultApplicationManagerImpl implements ApplicationManager, ApplicationContextAware {
    @Autowired
    @Lazy
    private BaseApplicationConfigDao baseApplicationConfigDao;

    public DbAppConfigManager(ChannelConfigProperties channelConfigProperties, SystemParameterRenameProperties systemParameterProperties) {
        super(channelConfigProperties, systemParameterProperties);
    }

    @Override
    public void validate(String channelId, String appId, String clientId, String versionCode) {

    }

    @Override
    @CacheMethodResult(keyMode = CacheKeyMode.CUSTOM_ID, id="channelId", expireSeconds=600)
    public ApiPermissionDto getChannelPermission(String channelId) {
        //支付中心暂时不开放渠道+应用模型控制应用方接口调用模式，因此沿用默认实现
        return super.getChannelPermission(channelId);
    }

    @Override
    @CacheMethodResult(keyMode = CacheKeyMode.CUSTOM_ID, id="appId", expireSeconds=600)
    public ApiPermissionDto getAppPermission(String appId) {
        BaseApplicationConfig applicationConfig = baseApplicationConfigDao.getByAppId(appId);
        if(applicationConfig == null){
            return super.getAppPermission(appId);
        }
        ApiPermissionDto permissionDto = new ApiPermissionDto();
        permissionDto.setSecret(applicationConfig.getSecret());
        SecretKeyPair keypair = new SecretKeyPair();
        keypair.setPrivateSecret(applicationConfig.getPrivateSecret());
        keypair.setPublicSecret(applicationConfig.getPublicSecret());
        permissionDto.setKeyPair(keypair);
        permissionDto.setEnable(applicationConfig.getEnable());
        if(StringUtils.isNotBlank(applicationConfig.getBlackIpList())){
            Set<String> ipList = new HashSet<>(Arrays.asList(applicationConfig.getBlackIpList().split(",")));
            permissionDto.setBlackAppIdList(ipList);
        }
        permissionDto.setTimeSection(applicationConfig.getTimeSection());
        permissionDto.setTotalMaxAccessCount(applicationConfig.getTotalMaxAccessCount());
        permissionDto.setUserMaxAccessCount(applicationConfig.getUserMaxAccessCount());
        permissionDto.setUserSessionMaxAccessCount(applicationConfig.getUserSessionMaxAccessCount());
        return permissionDto;
    }

    @Override
    @CacheMethodResult(keyMode = CacheKeyMode.CUSTOM_ID, id="appId", expireSeconds=600)
    public ApiPermissionDto getClientPermission(String appId, String versionCode) {
        return getAppPermission(appId);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        super.channelConfigProperties = applicationContext.getBean(ChannelConfigProperties.class);
        super.systemParameterProperties = applicationContext.getBean(SystemParameterRenameProperties.class);
    }
}
