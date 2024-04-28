package com.pddon.framework.easyapi.dao;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.entity.PartnerItem;
import com.pddon.framework.easyapi.dto.req.PartnerListRequest;

import java.util.List;

/**
 * @ClassName: PartnerItemDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 22:24
 * @Addr: https://pddon.cn
 */
public interface PartnerItemDao {

    boolean saveItem(PartnerItem item);

    PartnerItem getByItemId(Long id);

    boolean updateByItemId(PartnerItem item);

    boolean removeByIds(List<String> ids);

    IPage<PartnerItem> pageQuery(PartnerListRequest req);

    boolean existsTenantId(String tenantId);

    boolean updateStatus(Long id, String name);
}
