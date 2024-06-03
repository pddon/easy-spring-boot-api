package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.HtmlPageService;
import com.pddon.framework.easyapi.dao.HtmlPageDao;
import com.pddon.framework.easyapi.dao.consts.HtmlPageStatus;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
