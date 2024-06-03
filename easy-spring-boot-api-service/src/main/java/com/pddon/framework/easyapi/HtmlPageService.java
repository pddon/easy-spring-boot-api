package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.dao.entity.HtmlPage;

/**
 * @ClassName: HtmlPageService
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:41
 * @Addr: https://pddon.cn
 */
public interface HtmlPageService {
    HtmlPage getByPagePath(String pagePath);
}
