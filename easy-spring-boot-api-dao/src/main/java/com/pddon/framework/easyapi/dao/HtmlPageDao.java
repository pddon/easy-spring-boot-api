package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.HtmlPage;

/**
 * @ClassName: HtmlPageDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 12:22
 * @Addr: https://pddon.cn
 */
public interface HtmlPageDao {
    HtmlPage getByPagePath(String pagePath);
}
