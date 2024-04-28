package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import com.pddon.framework.easyapi.dao.entity.BaseApplicationConfig;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName: ApplicationConfigDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-03-01 22:04
 * @Addr: https://pddon.cn
 */
public interface BaseApplicationConfigDao<K extends BaseApplicationConfig> {
    K getByAppId(String appId);

    boolean saveItem(BaseApplicationConfig applicationConfig);

    K getByItemId(@NotNull Long id);

    boolean updateByItemId(BaseApplicationConfig config);

    boolean removeByAppIds(List<String> asList);

    IPage<K> pageQuery(PaginationRequest req, String tenantId, String keyword);

    boolean existsAppId(String appId);
}
