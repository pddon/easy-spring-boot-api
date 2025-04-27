package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.DepartmentDao;
import com.pddon.framework.easyapi.dao.entity.Department;
import com.pddon.framework.easyapi.dao.mapper.DepartmentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @ClassName: DepartmentDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-27 23:07
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class DepartmentDaoImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentDao {


}
