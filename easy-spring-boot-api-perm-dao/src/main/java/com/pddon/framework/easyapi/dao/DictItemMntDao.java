package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dao.dto.request.DictListRequest;

import java.util.List;
import java.util.Set;

/**
 * @ClassName: DictItemMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 00:58
 * @Addr: https://pddon.cn
 */
public interface DictItemMntDao extends DictItemDao {
    boolean exists(String tenantId, String appId, String userId, String groupId, String dictId);

    boolean exists(Long id);

    boolean exists(Long id, String dictId);

    boolean saveItem(DictItem item);

    DictItem getByItemId(Long id);

    boolean updateByItemId(DictItem item);

    boolean removeByIds(List<String> ids);

    boolean saveOrUpdateItem(DictItem dictItem);

    List<DictItem> getByGroupId(String groupId);

    List<DictItem> getByTenantGroupId(String tenantId, String groupId);

    List<DictItem> getByItemIds(Set<Long> keySet);

    void saveOrUpdateByItemIds(List<DictItem> dictItems);
}
