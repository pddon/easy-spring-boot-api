package com.pddon.framework.easyapi.dao.entity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class BaseEntity extends BaseHardEntity {
    /**
     * 逻辑删除控制
     * @author pddon.com
     */
    @TableLogic
    private Boolean deleted;
}
