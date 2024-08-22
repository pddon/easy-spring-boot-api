package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.HtmlPageMntService;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.request.IdRequest;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName: HtmlPageController
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:44
 * @Addr: https://pddon.cn
 */
@Api(tags = "html页面相关管理接口")
@RestController
@RequestMapping("page")
@Slf4j
public class HtmlPageController {

    @Autowired
    private HtmlPageMntService htmlPageMntService;

    @PostMapping("add")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增html页面", apiName = "page/add")
    @RequiresPermissions("page:add")
    public IdResponse add(@RequestBody AddHtmlPageRequest req){
        return htmlPageMntService.add(req);
    }

    @PostMapping("update")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改html页面", apiName = "page/update")
    @RequiresPermissions("page:update")
    public void update(@RequestBody UpdateHtmlPageRequest req){
        htmlPageMntService.update(req);
    }

    @PostMapping("delete")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除html页面", apiName = "page/delete")
    @RequiresPermissions("page:delete")
    public void remove(@RequestBody IdsRequest req){
        htmlPageMntService.remove(req);
    }

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiresPermissions("page:query")
    public PaginationResponse<HtmlPage> list(@RequestBody HtmlPageListRequest req){
        return htmlPageMntService.list(req);
    }

    @GetMapping("get")
    @RequiredSign(scope = SignScope.REQUEST)
    //@OperateLog(type="获取html页面内容", apiName = "page/get")
    @RequiresPermissions("page:query")
    public HtmlPage get(@RequestParam(name = "id") Long id){
        return htmlPageMntService.getPageContent(id);
    }

    @PostMapping("deploy")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="部署html页面到服务器", apiName = "page/deploy")
    @RequiresPermissions("page:update")
    public void deploy(@RequestParam(name = "id") Long id){
        htmlPageMntService.deployPage(id);
    }

    @PostMapping("top")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="置顶html页面", apiName = "page/top")
    @RequiresPermissions("page:update")
    public void top(@RequestParam(name = "id") Long id){
        htmlPageMntService.topPage(id);
    }

}
