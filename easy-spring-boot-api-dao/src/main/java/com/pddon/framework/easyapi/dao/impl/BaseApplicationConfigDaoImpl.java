package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.controller.request.PaginationRequest;
import com.pddon.framework.easyapi.dao.BaseApplicationConfigDao;
import com.pddon.framework.easyapi.dao.entity.BaseApplicationConfig;
import com.pddon.framework.easyapi.dao.mapper.BaseApplicationConfigMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @ClassName: BaseApplicationConfigDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
public abstract class BaseApplicationConfigDaoImpl<T extends BaseApplicationConfigMapper<K>, K extends BaseApplicationConfig> extends ServiceImpl<T, K> implements BaseApplicationConfigDao<K> {

    @Override
    public K getByAppId(String appId) {
        Wrapper<K> wrapper = new LambdaQueryWrapper<K>()
                .eq(BaseApplicationConfig::getAppId, appId)
                .ne(BaseApplicationConfig::getDeleted , 1);
        return super.getOne(wrapper);
    }

    abstract protected K newEntityInstance();

    @Override
    public boolean saveItem(BaseApplicationConfig applicationConfig) {
        K item = this.newEntityInstance();
        BeanUtils.copyProperties(applicationConfig, item);
        return this.save(item);
    }

    @Override
    public K getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateByItemId(BaseApplicationConfig config) {
        K item = this.newEntityInstance();
        BeanUtils.copyProperties(config, item);
        return this.updateById(item);
    }

    @Override
    public boolean removeByAppIds(List<String> list) {
        return this.remove(new LambdaQueryWrapper<K>().in(BaseApplicationConfig::getId, list));
    }

    @Override
    public IPage<K> pageQuery(PaginationRequest req, String tenantId, String keyword, String appType) {
        Page<K> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认降序排列
            req.setIsAsc(false);
        }
        Wrapper<K> wrapper = new LambdaQueryWrapper<K>()
                .eq(!StringUtils.isEmpty(tenantId), BaseApplicationConfig::getTenantId, tenantId)
                .eq(!StringUtils.isEmpty(appType), BaseApplicationConfig::getAppType, appType)
                .and(!StringUtils.isEmpty(keyword), query -> {
                    return query.likeRight(BaseApplicationConfig::getAppName, keyword).or()
                            .likeRight(BaseApplicationConfig::getDescription, keyword);
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? BaseApplicationConfig::getCrtTime : BaseApplicationConfig::getChgTime);
        return this.page(page, wrapper);
    }

    @Override
    public boolean existsAppId(String appId) {
        return this.count(new LambdaQueryWrapper<K>().eq(BaseApplicationConfig::getAppId, appId)) > 0;
    }
}
