package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dto.req.AddHtmlPageRequest;
import com.pddon.framework.easyapi.dto.req.HtmlPageListRequest;
import com.pddon.framework.easyapi.dto.req.UpdateHtmlPageRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;

/**
 * @ClassName: HtmlPageMntService
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:42
 * @Addr: https://pddon.cn
 */
public interface HtmlPageMntService {
    IdResponse add(AddHtmlPageRequest req);

    void update(UpdateHtmlPageRequest req);

    void remove(IdsRequest req);

    PaginationResponse<HtmlPage> list(HtmlPageListRequest req);

    HtmlPage getPageContent(Long id);

    void deployPage(Long id);

    void topPage(Long id);
}
