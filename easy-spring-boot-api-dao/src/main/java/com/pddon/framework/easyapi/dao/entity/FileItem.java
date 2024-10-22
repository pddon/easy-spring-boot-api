package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: FileItem
 * @Description: 文件记录
 * @Author: Allen
 * @Date: 2024-06-24 17:02
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("file_item")
public class FileItem extends BaseHardTenantEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 文件Id
     * @author pddon.com
     */
    private String fileKey;
    /**
     * 文件名
     * @author pddon.com
     */
    private String filename;
    /**
     * 文件类型
     * @author pddon.com
     */
    private String fileType;
    /**
     * 文件类型
     * @author pddon.com
     */
    private String contentType;
    /**
     * 存储类型
     * @author pddon.com
     */
    private String storeType;
    /**
     * 访问地址
     * @author pddon.com
     */
    private String fileUrl;
    /**
     * 图片数据
     * @author pddon.com
     */
    private byte[] fileData;
    /**
     * 是否禁用
     * @author pddon.com
     */
    private Boolean disabled;
    /**
     * 使用次数，使用次数为0时可以删除
     */
    private Integer useCount;
    /**
     * 备注信息
     * @author pddon.com
     */
    private String comments;
}
