package com.pddon.framework.easyapi.consts;

import lombok.Getter;

/**
 * @ClassName: ApiPermOperateType
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-29 00:18
 * @Addr: https://pddon.cn
 */
@Getter
public enum ApiPermOperateType {
    ADD("add", "新增"),
    DELETE("delete", "删除"),
    UPDATE("update", "更新"),
    QUERY("query", "查询");

    private String key;
    private String name;

    ApiPermOperateType(String key, String name){
        this.key = key;
        this.name = name;
    }
}
