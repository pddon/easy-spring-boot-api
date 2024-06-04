package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.HtmlPageDao;
import com.pddon.framework.easyapi.dao.HtmlSegmentDao;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dao.mapper.HtmlPageMapper;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public List<HtmlPage> getListBySceneId(String sceneId, String resourceId) {
        return this.lambdaQuery().eq(HtmlPage::getPageBusinessId, sceneId)
                .eq(StringUtils.isNotEmpty(resourceId), HtmlPage::getResourceId, resourceId)
                .orderByDesc(HtmlPage::getChgTime).list();
    }

    @Override
    public List<HtmlPage> getListByKeyword(String sceneId, String keyword) {
        return this.lambdaQuery().eq(StringUtils.isNotEmpty(sceneId), HtmlPage::getPageBusinessId, sceneId)
                .and(query -> {
                    return query.like(HtmlPage::getTitle, keyword).or()
                            .like(HtmlPage::getKeywords, keyword).or()
                            .like(HtmlPage::getDescription, keyword).or();
                }).list();
    }
}
