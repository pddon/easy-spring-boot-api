package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.RolePermDao;
import com.pddon.framework.easyapi.dao.entity.RolePerm;
import com.pddon.framework.easyapi.dao.mapper.RolePermMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @ClassName: RolePermDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class RolePermDaoImpl extends ServiceImpl<RolePermMapper, RolePerm> implements RolePermDao {

}
