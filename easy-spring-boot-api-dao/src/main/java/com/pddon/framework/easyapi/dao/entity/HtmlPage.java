package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: HtmlPage
 * @Description: html静态页面，支持部署到静态页面发布目录
 * @Author: Allen
 * @Date: 2024-05-29 11:43
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("html_page")
public class HtmlPage extends BaseEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;
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
    private String title;
    /**
     * 页面关键字，以英文逗号分隔
     * @author pddon.com
     */
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
    private String description;
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
     * 页面状态 取值：EDIT 编辑中， COMPLETE 已完成， DEPLOYED 已部署， OFFLINE 已下线
     * @author pddon.com
     */
    private String pageStatus;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String comments;
    /**
     * 排序值，值越大排名越靠前
     * @author pddon.com
     */
    private Long orderValue;

}
