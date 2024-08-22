package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.HtmlPageService;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.HtmlPageDao;
import com.pddon.framework.easyapi.dao.consts.HtmlPageStatus;
import com.pddon.framework.easyapi.dao.dto.request.HtmlPageListRequest;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dto.HtmlPageContentDto;
import com.pddon.framework.easyapi.dto.HtmlPageDetailDto;
import com.pddon.framework.easyapi.dto.HtmlPageDto;
import com.pddon.framework.easyapi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName: HtmlPageServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:41
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class HtmlPageServiceImpl implements HtmlPageService {
    @Autowired
    private HtmlPageDao htmlPageDao;

    @Override
    public HtmlPage getByPagePath(String pagePath) {
        HtmlPage htmlPage = htmlPageDao.getByPagePath(pagePath);
        if(htmlPage == null){
            throw new BusinessException("页面未找到!");
        }
        if(!HtmlPageStatus.DEPLOYED.name().equalsIgnoreCase(htmlPage.getPageStatus())){
            throw new BusinessException("您访问的页面当前不可见!");
        }
        return htmlPage;
    }

    @Override
    public List<HtmlPageDto> getPagesByScene(String sceneId, String resourceId) {
        List<HtmlPage> pages = htmlPageDao.getListBySceneId(sceneId, resourceId);

        return pages.stream().map(page -> {
            HtmlPageDto dto = new HtmlPageDto();
            BeanUtils.copyProperties(page, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<HtmlPageDto> searchPage(String sceneId, String keyword) {
        List<HtmlPage> pages = htmlPageDao.getListByKeyword(sceneId, keyword);

        return pages.stream().map(page -> {
            HtmlPageDto dto = new HtmlPageDto();
            BeanUtils.copyProperties(page, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public HtmlPageContentDto getPageByResId(String sceneId, String resourceId) {
        List<HtmlPage> pages = htmlPageDao.getListBySceneId(sceneId, resourceId);
        if(pages.isEmpty()){
            throw new BusinessException("未找到页面内容!");
        }
        HtmlPageContentDto dto = new HtmlPageContentDto();
        BeanUtils.copyProperties(pages.get(0), dto);
        return dto;
    }

    @Override
    public PaginationResponse<HtmlPageDto> list(HtmlPageListRequest req) {
        IPage<HtmlPage> itemPage = htmlPageDao.pageQuery(req);
        PaginationResponse<HtmlPageDto> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords().stream().map(item -> {
                    HtmlPageDto dto = new HtmlPageDto();
                    BeanUtils.copyProperties(item, dto);
                    return dto;
                }).collect(Collectors.toList()));
        return page;
    }

    @Override
    public HtmlPageDetailDto getPageById(Long id) {
        HtmlPage page = htmlPageDao.getByItemId(id);
        if(page == null || !HtmlPageStatus.DEPLOYED.name().equals(page.getPageStatus())){
            throw new BusinessException("页面内容未找到!");
        }
        HtmlPageDetailDto dto = new HtmlPageDetailDto();
        BeanUtils.copyProperties(page, dto);
        return dto;
    }
}
