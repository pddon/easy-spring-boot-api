package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName: BaseLogicEntity
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-17 23:50
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain=true)
public class BaseHardEntity implements Serializable {
    /**
     * 版本号，用于乐观锁控制
     * @author pddon.com
     */
    @Version
    private Integer version;

    /**
     * 记录创建时间
     * @author pddon.com
     */
    private Date crtTime;

    /**
     * 记录最近被修改的时间
     * @author pddon.com
     */
    private Date chgTime;

    /**
     * 记录被创建的用户ID
     * @author pddon.com
     */
    private String crtUserId;

    /**
     * 最近一次修改此条记录的用户ID
     * @author pddon.com
     */
    private String chgUserId;
}
