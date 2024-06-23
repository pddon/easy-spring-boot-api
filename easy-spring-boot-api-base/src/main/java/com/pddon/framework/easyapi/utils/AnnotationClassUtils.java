package com.pddon.framework.easyapi.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName: AnnotationClassUtils
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-11 22:18
 * @Addr: https://pddon.cn
 */
@Slf4j
public class AnnotationClassUtils {

    public static Map<Class<?>, Annotation> findAnnotations(Class<?> clazz) {
        List<Annotation> annotations = new ArrayList<>();
        Annotation[] annos = clazz.getAnnotations();
        if(annos != null){
            annotations.addAll(Arrays.stream(annos).collect(Collectors.toList()));
        }
        return annotations.stream().collect(Collectors.toMap(annotation -> annotation.annotationType(), annotation -> annotation, (item1, item2) -> item2));
    }

    public static Map<Class<?>, Annotation> findFieldAnnotations(Class<?> clazz, String fieldName, boolean containsClazz) {
        List<Annotation> annotations = new ArrayList<>();
        if(containsClazz){
            Annotation[] annos = clazz.getAnnotations();
            if(annos != null){
                annotations.addAll(Arrays.stream(annos).collect(Collectors.toList()));
            }
        }
        Field field = ReflectionUtils.findField(clazz, fieldName);
        if(field != null){
            Annotation[] subAnnos = field.getAnnotations();
            if(subAnnos != null){
                annotations.addAll(Arrays.stream(subAnnos).collect(Collectors.toList()));
            }
        }
        return annotations.stream().collect(Collectors.toMap(annotation -> annotation.annotationType(), annotation -> annotation, (item1, item2) -> item2));
    }

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
