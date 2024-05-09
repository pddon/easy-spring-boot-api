package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.DictGroupDao;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dto.req.DictGroupListRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;

import java.util.List;

/**
 * @ClassName: DictGroupMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 00:58
 * @Addr: https://pddon.cn
 */
public interface DictGroupMntDao extends DictGroupDao {
    boolean exists(String groupId);

    boolean exists(Long id);

    boolean exists(Long id, String groupId);

    boolean saveItem(DictGroup item);

    DictGroup getByItemId(Long id);

    boolean updateByItemId(DictGroup item);

    boolean removeByIds(List<String> asList);

    IPage<DictGroup> pageQuery(DictGroupListRequest req);
}
