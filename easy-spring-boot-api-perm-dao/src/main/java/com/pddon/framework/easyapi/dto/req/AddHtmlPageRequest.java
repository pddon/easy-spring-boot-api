package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.annotation.IgnoreSign;
import com.pddon.framework.easyapi.dao.consts.HtmlPageStatus;
import com.pddon.framework.easyapi.dao.consts.PageContentType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName: AddHtmlPageRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 23:56
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class AddHtmlPageRequest implements Serializable {
    /**
     * 页面所属业务场景，字典分组pageBusinessId下的子分组列表，可以通过子字典分组新增业务应用场景
     * @author pddon.com
     */
    private String pageBusinessId;
    /**
     * 页面所属场景下特定的资源专属使用，通过新增pageBusinessId的子分组下的字典，新增资源ID
     * @author pddon.com
     */
    private String resourceId;
    /**
     * 页面访问路径，相对于服务器静态资源部署的根路径地址
     * @author pddon.com
     */
    private String urlPath;
    /**
     * 页面标题
     * @author pddon.com
     */
    @NotEmpty
    private String title;
    /**
     * 页面关键字，以英文逗号分隔
     * @author pddon.com
     */
    @NotEmpty
    private String keywords;
    /**
     * 封面图ID
     * @author pddon.com
     */
    private String imgKey;
    /**
     * 封面图访问地址
     * @author pddon.com
     */
    private String imgUrl;
    /**
     * 描述
     * @author pddon.com
     */
    @NotEmpty
    private String description;
    /**
     * 内容类型 取值：HTML html代码、MARKDOWN markdown格式文本
     * @author pddon.com
     */
    @NotNull
    private PageContentType type;
    /**
     * 页面最终html内容
     * @author pddon.com
     */
    @NotEmpty
    @IgnoreSign
    private String content;
    /**
     * 页面编辑内容，用于存储非最终html的设计稿内容
     * @author pddon.com
     */
    @NotEmpty
    @IgnoreSign
    private String editableContent;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String comments;
    /**
     * 是否立即部署
     */
    @NotNull
    private Boolean deployNow;
    /**
     * 是否已经编辑完成
     */
    @NotNull
    private Boolean completed;
}
