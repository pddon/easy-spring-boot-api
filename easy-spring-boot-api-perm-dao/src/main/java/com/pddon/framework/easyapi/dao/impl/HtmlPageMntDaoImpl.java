package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pddon.framework.easyapi.dao.HtmlPageMntDao;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dto.req.HtmlPageListRequest;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName: HtmlPageMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 23:38
 * @Addr: https://pddon.cn
 */
@Service
@Primary
@Slf4j
public class HtmlPageMntDaoImpl extends HtmlPageDaoImpl implements HtmlPageMntDao {
    @Override
    public boolean exists(String urlPath) {
        return this.lambdaQuery().eq(HtmlPage::getUrlPath, urlPath).count() > 0;
    }

    @Override
    public boolean saveItem(HtmlPage page) {
        return this.save(page);
    }

    @Override
    public HtmlPage getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateByItemId(HtmlPage page) {
        return this.updateById(page);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return this.remove(new LambdaQueryWrapper<HtmlPage>().in(HtmlPage::getId, ids));
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
    public void topPage(Long id) {
        this.lambdaUpdate().eq(HtmlPage::getId, id).set(HtmlPage::getOrderValue, System.currentTimeMillis()).update();
    }
}
