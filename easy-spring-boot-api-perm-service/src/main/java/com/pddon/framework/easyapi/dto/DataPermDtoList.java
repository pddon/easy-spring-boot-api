package com.pddon.framework.easyapi.dto;

import com.pddon.framework.easyapi.dao.dto.DataPermDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: DataPermDtoList
 * @Description:
 * @Author: Allen
 * @Date: 2025-05-15 22:33
 * @Addr: https://pddon.cn
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataPermDtoList implements Serializable {
    private List<DataPermDto> items;
}
