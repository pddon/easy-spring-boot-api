package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dto.HtmlPageContentDto;
import com.pddon.framework.easyapi.dto.HtmlPageDto;

import java.util.List;

/**
 * @ClassName: HtmlPageService
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:41
 * @Addr: https://pddon.cn
 */
public interface HtmlPageService {
    HtmlPage getByPagePath(String pagePath);

    List<HtmlPageDto> getPagesByScene(String sceneId, String resourceId);

    List<HtmlPageDto> searchPage(String sceneId, String keyword);

    HtmlPageContentDto getPageByResId(String sceneId, String resourceId);
}
