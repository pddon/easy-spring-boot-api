package com.pddon.framework.easyapi.email.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class EmailAuthTokenDto implements Serializable {

    private String token;

    private Long crtTime;
}
