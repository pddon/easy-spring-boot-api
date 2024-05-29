package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.HtmlSegmentDao;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dao.entity.HtmlSegment;
import com.pddon.framework.easyapi.dao.mapper.DictItemMapper;
import com.pddon.framework.easyapi.dao.mapper.HtmlPageMapper;
import com.pddon.framework.easyapi.dao.mapper.HtmlSegmentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @ClassName: HtmlSegmentDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:34
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class HtmlSegmentDaoImpl extends ServiceImpl<HtmlSegmentMapper, HtmlSegment> implements HtmlSegmentDao {

}
