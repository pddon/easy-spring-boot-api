package com.pddon.framework.easyapi.dao.consts;

/**
 * @ClassName: UserAccountStatus
 * @Description: 用户账号状态
 * @Author: Allen
 * @Date: 2024-04-15 22:18
 * @Addr: https://pddon.cn
 */
public enum UserAccountStatus {

    /**
     * 正常使用状态
     */
    ACTIVE,
    /**
     * 被冻结，无法使用
     */
    FROZEN,
    /**
     * 被禁用，无法使用
     */
    DISABLE
}
