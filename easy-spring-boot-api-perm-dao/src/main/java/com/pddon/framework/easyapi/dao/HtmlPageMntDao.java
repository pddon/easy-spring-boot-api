package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dto.req.HtmlPageListRequest;

import java.util.List;

/**
 * @ClassName: HtmlPageMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 23:37
 * @Addr: https://pddon.cn
 */
public interface HtmlPageMntDao {
    boolean exists(String urlPath);

    boolean saveItem(HtmlPage page);

    HtmlPage getByItemId(Long id);

    boolean updateByItemId(HtmlPage page);

    boolean removeByIds(List<String> asList);

    IPage<HtmlPage> pageQuery(HtmlPageListRequest req);

    void topPage(Long id);
}
