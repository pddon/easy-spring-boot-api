package com.pddon.framework.easyapi.dao.consts;

/**
 * @ClassName: FileStoreType
 * @Description: 文件数据存储类型
 * @Author: Allen
 * @Date: 2024-06-24 17:06
 * @Addr: https://pddon.cn
 */
public enum FileStoreType {
    /**
     * 存储在数据库中
     */
    IN_DB,
    ALI_CLOUD_OSS,
    AWS_S3;
}
