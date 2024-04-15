package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.PermItemDao;
import com.pddon.framework.easyapi.dao.entity.PermItem;
import com.pddon.framework.easyapi.dao.mapper.PermItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @ClassName: PermItemDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class PermItemDaoImpl extends ServiceImpl<PermItemMapper, PermItem> implements PermItemDao {

}
