package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.HtmlPageDao;
import com.pddon.framework.easyapi.dao.HtmlSegmentDao;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dao.mapper.HtmlPageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @ClassName: HtmlPageDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:34
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class HtmlPageDaoImpl extends ServiceImpl<HtmlPageMapper, HtmlPage> implements HtmlPageDao {

    @Override
    public HtmlPage getByPagePath(String pagePath) {
        return this.lambdaQuery().eq(HtmlPage::getUrlPath, pagePath).one();
    }
}
