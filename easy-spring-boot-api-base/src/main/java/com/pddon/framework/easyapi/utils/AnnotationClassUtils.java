package com.pddon.framework.easyapi.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @ClassName: AnnotationClassUtils
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-11 22:18
 * @Addr: https://pddon.cn
 */
@Slf4j
public class AnnotationClassUtils {

    public static List<Class<?>> getAllClassByAnnotation(String packageName, Class<? extends Annotation> tClass){
        // 创建扫描器
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        // 添加注解过滤器，这里以@Service为例
        provider.addIncludeFilter(new AnnotationTypeFilter(tClass));
        // 设置扫描的基础包，这里以com.example为例
        provider.setResourceLoader(null);

        List<Class<?>> scanClassList = new ArrayList<>();
        // 获取扫描到的类名
        Set<BeanDefinition> candidateComponents = provider.findCandidateComponents(packageName);
        for (BeanDefinition beanDef : candidateComponents) {
            Class<?> cls = null;
            try {
                cls = Class.forName(beanDef.getBeanClassName());
            } catch (ClassNotFoundException e) {
                log.warn(IOUtils.getThrowableInfo(e));
            }
            scanClassList.add(cls);
        }
        return scanClassList;
    }
}
