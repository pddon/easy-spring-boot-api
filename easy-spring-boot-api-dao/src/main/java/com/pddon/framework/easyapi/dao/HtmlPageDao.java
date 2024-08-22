package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.dto.request.HtmlPageListRequest;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;

import java.util.List;

/**
 * @ClassName: HtmlPageDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 12:22
 * @Addr: https://pddon.cn
 */
public interface HtmlPageDao {
    HtmlPage getByPagePath(String pagePath);

    List<HtmlPage> getListBySceneId(String sceneId, String resourceId);

    List<HtmlPage> getListByKeyword(String sceneId, String keyword);

    IPage<HtmlPage> pageQuery(HtmlPageListRequest req);

    HtmlPage getByItemId(Long id);
}
