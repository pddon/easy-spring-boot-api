package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: HtmlSegment
 * @Description: html片段，支持嵌入html页面区块
 * @Author: Allen
 * @Date: 2024-05-29 11:43
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("html_segment")
public class HtmlSegment extends BaseTenantEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 页面片段所属应用ID
     * @author pddon.com
     */
    private String appId;
    /**
     * 页面片段应用场景，字典分组pageSegmentId下的子分组列表，可以通过子字典分组新增业务应用场景
     * @author pddon.com
     */
    private String pageSegmentId;
    /**
     * 页面片段应用场景下特定的资源专属使用，通过新增pageSegmentId的子分组下的字典，新增资源ID
     * @author pddon.com
     */
    private String resourceId;
    /**
     * 片段标题
     * @author pddon.com
     */
    private String title;
    /**
     * 内容类型 取值：HTML html代码、MARKDOWN markdown格式文本
     * @author pddon.com
     */
    private String contentType;
    /**
     * 页面最终html内容
     * @author pddon.com
     */
    private String content;
    /**
     * 页面编辑内容，用于存储非最终html的设计稿内容
     * @author pddon.com
     */
    private String editableContent;
    /**
     * css样式内容
     * @author pddon.com
     */
    private String cssContent;
    /**
     * js可执行脚本内容
     * @author pddon.com
     */
    private String jsContent;
    /**
     * 是否启用片段
     * @author pddon.com
     */
    private Boolean enabled;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String comments;

}
