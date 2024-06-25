package com.pddon.framework.easyapi.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @ClassName: FileUploadResult
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-24 19:03
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class FileUploadResult implements Serializable {

    private String fileKey;

    private String filename;

    private String fileUrl;
}
