/**  
* Title DefaultApplicationManagerImpl.java  
* Description  
* @author danyuan
* @date Nov 1, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.ApplicationManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.ApiPermissionDto;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.properties.ChannelConfigProperties;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
public class DefaultApplicationManagerImpl implements ApplicationManager {

	protected ChannelConfigProperties channelConfigProperties;

	protected SystemParameterRenameProperties systemParameterProperties;


	/**
	 * @author danyuan
	 */
	@Override
	public void validate(String channelId, String appId, String clientId,
			String versionCode) {
		//TODO: 待补充
	}

	/**
	 * @author danyuan
	 */
	@Override
	public ApiPermissionDto getChannelPermission(String channelId) {
		if(channelConfigProperties.getChannels() != null){//渠道配置存在
			if(StringUtils.isNotBlank(channelId)){
				if(!channelConfigProperties.getChannels().containsKey(channelId)){
					throw new BusinessException(ErrorCodes.INVALID_PARAM).setParam(systemParameterProperties.getChannelId(), channelId);
				}
			}else{//没有传递渠道参数的请求使用默认渠道配置
				channelId = ChannelConfigProperties.DEFAULT;
			}

			Map<String, ApiPermissionDto> apps = channelConfigProperties.getChannels().get(channelId);
			if(apps == null){
				throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam("渠道秘钥信息");
			}
			ApiPermissionDto permission = apps.get(RequestContext.getContext().getAppId());
			if(permission == null){
				//如果不存在单个应用的配置信息，则使用渠道默认配置
				permission = apps.get(ChannelConfigProperties.DEFAULT);
			}
			return permission;
		}
		return null;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public ApiPermissionDto getAppPermission(String appId) {
		if(channelConfigProperties.getChannels() != null){//渠道配置存在
			String channelId = ChannelConfigProperties.DEFAULT;

			Map<String, ApiPermissionDto> apps = channelConfigProperties.getChannels().get(channelId);
			if(apps == null){
				throw new BusinessException(ErrorCodes.NOT_FOUND_CONFIG).setParam("渠道秘钥信息");
			}
			return apps.get(RequestContext.getContext().getAppId());
		}
		return null;
	}

	/**
	 * @author danyuan
	 */
	@Override
	public ApiPermissionDto getClientPermission(String appId,
			String versionCode) {
		return getAppPermission(appId);
	}

}
