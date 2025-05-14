package com.pddon.framework.easyapi.dao.tenant;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.config.MybatisPlusConfig;
import com.pddon.framework.easyapi.utils.AnnotationClassUtils;
import com.pddon.framework.easyapi.utils.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName: EntityManager
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-11 22:16
 * @Addr: https://pddon.cn
 */
@Configuration
@AutoConfigureBefore(MybatisPlusConfig.class)
@Slf4j
public class EntityManager {

    private static final String TENANT_ID_FIELD_NAME = "tenantId";
    private static final Set<String> ignoreTenantTableSet = new HashSet<>();
    private static final Map<String, Class<?>> allTableMap = new HashMap<>();
    @Autowired
    @Lazy
    private MybatisPlusProperties mybatisPlusProperties;

    @PostConstruct
    public void init(){
        init(this.mybatisPlusProperties);
    }

    public synchronized static void init(MybatisPlusProperties mybatisPlusProperties){

        List<Class<?>> list = AnnotationClassUtils.getAllClassByAnnotation(mybatisPlusProperties.getTypeAliasesPackage(), TableName.class);
        list.forEach(clazz -> {
            // 获取表信息
            TableInfo table = TableInfoHelper.getTableInfo(clazz);
            if(table == null){
                return;
            }
            String tableName = table.getTableName();
            allTableMap.put(tableName, clazz);
            //标记表是否属于租户表
            IgnoreTenant ignoreTenant = AnnotationUtils.findAnnotation(clazz, IgnoreTenant.class);
            if((ignoreTenant != null && ignoreTenant.value()) || (ReflectionUtils.findField(clazz, TENANT_ID_FIELD_NAME) == null)){
                ignoreTenantTableSet.add(tableName);
            }
        });
    }

    public static TableInfo getTableInfo(Class<?> clazz){
        return TableInfoHelper.getTableInfo(clazz);
    }

    public static Class<?> getByTableName(String tableName){
        return allTableMap.get(tableName);
    }

    public static boolean isIgnoreTable(String tableName){
        Class<?> entityClass = getByTableName(tableName);
        if(entityClass != null && (ReflectionUtils.findField(entityClass, TENANT_ID_FIELD_NAME) == null)){
            return true;
        }
        if(RequestContext.getContext().isSuperManager() || RequestContext.getContext().isIgnoreTenant()){
            return true;
        }
        MybatisPlusProperties mybatisPlusProperties = SpringBeanUtil.getBean(MybatisPlusProperties.class);
        if(null == mybatisPlusProperties){
            //多租户插件还没初始化成功，直接忽略所有表
            return true;
        }else if(allTableMap.isEmpty()){
            //初始化
            init(mybatisPlusProperties);
        }
        return ignoreTenantTableSet.contains(tableName);
    }

    public static List<TableInfo> getAllTableInfos(){
        MybatisPlusProperties mybatisPlusProperties = SpringBeanUtil.getBean(MybatisPlusProperties.class);
        if(mybatisPlusProperties == null){
            return Collections.emptyList();
        }
        List<Class<?>> list = AnnotationClassUtils.getAllClassByAnnotation(mybatisPlusProperties.getTypeAliasesPackage(), TableName.class);
        return list.stream().map(clazz -> {
            // 获取表信息
            return TableInfoHelper.getTableInfo(clazz);
        }).collect(Collectors.toList()).stream().filter(item -> item != null).collect(Collectors.toList());
    }
}
