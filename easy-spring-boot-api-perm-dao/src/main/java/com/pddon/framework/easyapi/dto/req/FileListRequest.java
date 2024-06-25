package com.pddon.framework.easyapi.dto.req;

import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @ClassName: FileListRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-25 21:46
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class FileListRequest extends PaginationRequest {
    private String type;
    private String keyword;

    private Date startTime;

    private Date endTime;
}
