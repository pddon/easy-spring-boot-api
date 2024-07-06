package com.pddon.framework.easyapi;

/**
 * @ClassName: EmailSendRecordService
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-01 10:38
 * @Addr: https://pddon.cn
 */
public interface EmailSendRecordService {
    void saveSuccessRecord(String userId, String sceneId, String resourceId, String email, String title, String content);

    void saveSuccessRecord(String userId, String email, String title, String content);

    void saveFailedRecord(String userId, String email, String title, String content, String errMsg);

    void saveFailedRecord(String userId, String sceneId, String resourceId, String email, String title, String content, String errMsg);

    void saveRecord(String userId, String sceneId, String resourceId, String email, String title, String content, boolean success, String errMsg);
}
