package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.BaseUserDao;
import com.pddon.framework.easyapi.dao.consts.UserKeyType;
import com.pddon.framework.easyapi.dao.dto.request.UserListRequest;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dao.mapper.BaseUserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @ClassName: BaseUserDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
public abstract class BaseUserDaoImpl<T extends BaseUserMapper<K>, K extends BaseUser> extends ServiceImpl<T, K> implements BaseUserDao<K> {

    protected abstract K newEntityInstance();

    @Override
    public K getBySessionId(String sessionId) {
        List<K> list = this.list(new LambdaQueryWrapper<K>().eq(BaseUser::getSessionId, sessionId));
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public BaseUser getByUserId(String userId) {
        List<K> list = this.list(new LambdaQueryWrapper<K>().eq(BaseUser::getUserId, userId));
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public BaseUser getByEmail(String email) {
        List<K> list = this.list(new LambdaQueryWrapper<K>().eq(BaseUser::getEmail, email).orderByDesc(BaseUser::getCrtTime));
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public boolean updateUserSession(String sessionId, Date loginTime, String userId) {
        return this.update(new LambdaUpdateWrapper<K>().eq(BaseUser::getUserId, userId)
                .set(BaseUser::getSessionId, sessionId)
                .set(BaseUser::getLastLoginTime, loginTime)
        );
    }

    @Override
    public boolean existUserId(String userId) {
        return this.count(new LambdaQueryWrapper<K>().eq(BaseUser::getUserId, userId)) > 0;
    }

    @Override
    public boolean saveUser(BaseUser user) {
        K entity = newEntityInstance();
        BeanUtils.copyProperties(user, entity);
        return this.save(entity);
    }

    @Override
    public IPage<K> pageQuery(UserListRequest req) {
        Page<K> page = new Page<>(req.getCurrent(), req.getSize());
        Wrapper<K> wrapper = new LambdaQueryWrapper<K>()
                .eq(req.getTenantId() != null, BaseUser::getTenantId, req.getTenantId())
                .eq(req.getAccountStatus() != null, BaseUser::getAccountStatus, req.getAccountStatus())
                .gt(req.getStartTime() != null, BaseUser::getCrtTime, req.getStartTime())
                .lt(req.getEndTime() != null, BaseUser::getCrtTime, req.getEndTime())
                .likeRight(!StringUtils.isEmpty(req.getKeyword()) && UserKeyType.USER_ID.equals(req.getType()), BaseUser::getUserId, req.getKeyword())
                .likeRight(!StringUtils.isEmpty(req.getKeyword()) && UserKeyType.USERNAME.equals(req.getType()), BaseUser::getUsername, req.getKeyword())
                .likeRight(!StringUtils.isEmpty(req.getKeyword()) && UserKeyType.NICKNAME.equals(req.getType()), BaseUser::getNickname, req.getKeyword())
                .likeRight(!StringUtils.isEmpty(req.getKeyword()) && UserKeyType.EMAIL.equals(req.getType()), BaseUser::getEmail, req.getKeyword())
                .likeRight(!StringUtils.isEmpty(req.getKeyword()) && UserKeyType.PHONE.equals(req.getType()), BaseUser::getPhone, req.getKeyword())
                .likeRight(!StringUtils.isEmpty(req.getKeyword()) && UserKeyType.INTRO.equals(req.getType()), BaseUser::getIntro, req.getKeyword())
                .and(!StringUtils.isEmpty(req.getKeyword()) && req.getType() == null, query -> {
                    return query.eq(BaseUser::getUserId, req.getKeyword()).or()
                            .eq(BaseUser::getUsername, req.getKeyword()).or()
                            .eq(BaseUser::getNickname, req.getKeyword()).or()
                            .eq(BaseUser::getEmail, req.getKeyword()).or()
                            .eq(BaseUser::getPhone, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? BaseUser::getCrtTime : BaseUser::getChgTime);
        return this.page(page, wrapper);
    }

    @Override
    public boolean existUsername(String username) {
        return this.count(new LambdaQueryWrapper<K>().eq(BaseUser::getUsername, username)) > 0;
    }

    @Override
    public BaseUser getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateUser(K user) {
        return this.updateById(user);
    }

    @Override
    public boolean delete(List<Long> ids) {
        return this.remove(new LambdaQueryWrapper<K>().in(BaseUser::getId, ids));
    }

    @Override
    public boolean updateUserPass(Long id, String newPassword) {
        return this.update(new LambdaUpdateWrapper<K>().eq(BaseUser::getId, id).set(BaseUser::getPassword, newPassword));
    }

    @Override
    public void updateLastLoginTime(String userId) {
        this.lambdaUpdate().eq(BaseUser::getUserId, userId).set(BaseUser::getLastLoginTime, new Date()).update();
    }

    @Override
    public boolean updateDepartmentId(Long departmentId, List<String> userIds) {
        return this.lambdaUpdate().set(BaseUser::getDepId, departmentId).in(BaseUser::getUserId, userIds).update();
    }
}
