package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.DictGroupDao;
import com.pddon.framework.easyapi.dao.DictItemDao;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dao.mapper.DictGroupMapper;
import com.pddon.framework.easyapi.dao.mapper.DictItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @ClassName: DictItemDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 00:55
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class DictItemDaoImpl extends ServiceImpl<DictItemMapper, DictItem> implements DictItemDao {
}
