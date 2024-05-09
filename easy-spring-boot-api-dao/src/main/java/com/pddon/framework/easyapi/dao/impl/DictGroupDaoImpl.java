package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.DictGroupDao;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.mapper.DictGroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @ClassName: DictGroupDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 00:55
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class DictGroupDaoImpl extends ServiceImpl<DictGroupMapper, DictGroup> implements DictGroupDao {
}
