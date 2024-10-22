package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.HtmlPageDao;
import com.pddon.framework.easyapi.dao.HtmlSegmentDao;
import com.pddon.framework.easyapi.dao.consts.HtmlPageStatus;
import com.pddon.framework.easyapi.dao.dto.request.HtmlPageListRequest;
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
                .orderByDesc(HtmlPage::getOrderValue).list();
    }

    @Override
    public List<HtmlPage> getListByKeyword(String sceneId, String keyword) {
        return this.lambdaQuery().eq(StringUtils.isNotEmpty(sceneId), HtmlPage::getPageBusinessId, sceneId)
                .eq(HtmlPage::getPageStatus, HtmlPageStatus.DEPLOYED.name())
                .and(query -> {
                    return query.like(HtmlPage::getTitle, keyword).or()
                            .like(HtmlPage::getKeywords, keyword).or()
                            .like(HtmlPage::getDescription, keyword).or();
                }).orderByDesc(HtmlPage::getOrderValue).list();
    }

    @Override
    public IPage<HtmlPage> pageQuery(HtmlPageListRequest req) {
        Page<HtmlPage> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认降序排列
            req.setIsAsc(false);
        }
        Wrapper<HtmlPage> wrapper = new LambdaQueryWrapper<HtmlPage>()
                .select(HtmlPage.class, tableFieldInfo -> {
                    if(tableFieldInfo.getColumn().equalsIgnoreCase("content")
                            || tableFieldInfo.getColumn().equalsIgnoreCase("editable_content")){
                        return false;
                    }
                    return true;
                })
                .eq(StringUtils.isNotEmpty(req.getPageBusinessId()), HtmlPage::getPageBusinessId, req.getPageBusinessId())
                .eq(StringUtils.isNotEmpty(req.getResourceId()), HtmlPage::getResourceId, req.getResourceId())
                .eq(StringUtils.isNotEmpty(req.getUrlPath()), HtmlPage::getUrlPath, req.getUrlPath())
                .eq(StringUtils.isNotEmpty(req.getPageStatus()), HtmlPage::getPageStatus, req.getPageStatus())
                .eq(StringUtils.isNotEmpty(req.getTenantId()), HtmlPage::getTenantId, req.getTenantId())
                .and(StringUtils.isNotEmpty(req.getKeyword()), query -> {
                    return query.like(HtmlPage::getPageBusinessId, req.getKeyword()).or()
                            .like(HtmlPage::getResourceId, req.getKeyword()).or()
                            .like(HtmlPage::getUrlPath, req.getKeyword()).or()
                            .like(HtmlPage::getKeywords, req.getKeyword()).or()
                            .like(HtmlPage::getDescription, req.getKeyword()).or()
                            .like(HtmlPage::getTitle, req.getKeyword()).or()
                            .like(HtmlPage::getComments, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), HtmlPage::getOrderValue, "crtTime".equals(req.getOrderBy()) ? HtmlPage::getCrtTime : HtmlPage::getChgTime);
        return this.page(page, wrapper);
    }

    @Override
    public HtmlPage getByItemId(Long id) {
        return this.getById(id);
    }
}
