package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.DictGroup;

import java.util.List;

/**
 * @ClassName: DictGroupDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 00:55
 * @Addr: https://pddon.cn
 */
public interface DictGroupDao {
    List<DictGroup> getByParentDictGroupId(String parentId);
}
