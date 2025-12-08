package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.HtmlPageMntService;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.HtmlPageMntDao;
import com.pddon.framework.easyapi.dao.consts.HtmlPageStatus;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dto.req.AddHtmlPageRequest;
import com.pddon.framework.easyapi.dto.req.HtmlPageListRequest;
import com.pddon.framework.easyapi.dto.req.UpdateHtmlPageRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.StringUtils;
import com.pddon.framework.easyapi.utils.UUIDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @ClassName: HtmlPageMntServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:42
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class HtmlPageMntServiceImpl implements HtmlPageMntService {

    @Autowired
    private HtmlPageMntDao htmlPageMntDao;

    @Override
    public IdResponse add(AddHtmlPageRequest req) {
        if(htmlPageMntDao.exists(req.getUrlPath())){
            throw new BusinessException("页面已存在!");
        }
        HtmlPage page = new HtmlPage();
        BeanUtils.copyProperties(req, page);
        page.setContentType(req.getType().name())
                .setPageStatus(req.getCompleted() == true ? HtmlPageStatus.COMPLETE.name() : HtmlPageStatus.EDIT.name());
        if(req.getDeployNow() != null && (req.getDeployNow() == true)){
            page.setPageStatus(HtmlPageStatus.DEPLOYED.name());
        }
        if(StringUtils.isEmpty(page.getUrlPath())){
            page.setUrlPath(UUIDGenerator.getUUID());
        }
        htmlPageMntDao.saveItem(page);
        return new IdResponse(page.getId());
    }

    @Override
    public void update(UpdateHtmlPageRequest req) {
        HtmlPage page = htmlPageMntDao.getByItemId(req.getId());
        if(page == null){
            throw new BusinessException("页面不存在!");
        }
        BeanUtils.copyProperties(req, page);
        if(req.getType() != null){
            page.setContentType(req.getType().name());
        }
        if(req.getCompleted() != null){
            page.setPageStatus(req.getCompleted() == true ? HtmlPageStatus.COMPLETE.name() : HtmlPageStatus.EDIT.name());
        }
        if(req.getDeployNow() != null && (req.getDeployNow() == true)){
            page.setPageStatus(HtmlPageStatus.DEPLOYED.name());
        }
        htmlPageMntDao.updateByItemId(page);
    }

    @Override
    public void remove(IdsRequest req) {
        htmlPageMntDao.removeByIds(Arrays.asList(req.getIds()));
    }

    @Override
    public PaginationResponse<HtmlPage> list(HtmlPageListRequest req) {
        IPage<HtmlPage> itemPage = htmlPageMntDao.pageQuery(req);
        PaginationResponse<HtmlPage> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public HtmlPage getPageContent(Long id) {
        return htmlPageMntDao.getByItemId(id);
    }

    @Override
    public void deployPage(Long id) {
        HtmlPage htmlPage = htmlPageMntDao.getByItemId(id);
        if(htmlPage == null){
            throw new BusinessException("页面不存在！");
        }
        //部署页面

        //部署成功后
        htmlPage.setPageStatus(HtmlPageStatus.DEPLOYED.name());
        htmlPageMntDao.updateByItemId(htmlPage);
    }

    @Override
    public void topPage(Long id) {
        htmlPageMntDao.topPage(id);
    }
}
