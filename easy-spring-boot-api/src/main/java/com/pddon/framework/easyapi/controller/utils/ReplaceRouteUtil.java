package com.pddon.framework.easyapi.controller.utils;

import com.pddon.framework.easyapi.annotation.ReplaceRoute;
import com.pddon.framework.easyapi.utils.AnnotationClassUtils;
import com.pddon.framework.easyapi.utils.IOUtils;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @ClassName: ReplaceRouteUtil
 * @Description: 路由替换工具类
 * @Author: Allen
 * @Date: 2024-03-18 21:46
 * @Addr: https://pddon.cn
 */
@Slf4j
public class ReplaceRouteUtil {
    private static Map<String, String> routerMap = new HashMap<>();

    public static void initRoute(List<String> packageList) {
        List<Class<?>> scanClassList = new ArrayList<>();
        for (String packageName : packageList) {
            scanClassList.addAll(AnnotationClassUtils.getAllClassByAnnotation(packageName, ReplaceRoute.class));
        }
        
        for (Class clazz : scanClassList) {
            ReplaceRoute ReplaceRoute = (ReplaceRoute) clazz.getAnnotation(ReplaceRoute.class);
            RequestMapping requestMapping = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
            String classRoute = "";
            if (requestMapping != null) {
                classRoute = requestMapping.value()[0];
            } else {
                classRoute = "";
            }
            List<Method> methodList = Arrays.asList(clazz.getDeclaredMethods());
            for (Method method : methodList) {
                PostMapping postMapping = method.getAnnotation(PostMapping.class);
                String methodRoute = "";
                if (postMapping != null) {
                    if(postMapping.value() != null && postMapping.value().length > 0){
                        methodRoute = postMapping.value()[0];
                    } else if (postMapping.value() != null && postMapping.path().length > 0) {
                        methodRoute = postMapping.path()[0];
                    }
                } else {
                    GetMapping getMapping = method.getAnnotation(GetMapping.class);
                    if (getMapping != null) {
                        if(getMapping.value() != null && getMapping.value().length > 0){
                            methodRoute = getMapping.value()[0];
                        } else if (getMapping.value() != null && getMapping.path().length > 0) {
                            methodRoute = getMapping.path()[0];
                        }
                    } else {
                        PutMapping putMapping = method.getAnnotation(PutMapping.class);
                        if (putMapping != null) {
                            if(putMapping.value() != null && putMapping.value().length > 0){
                                methodRoute = putMapping.value()[0];
                            } else if (putMapping.value() != null && putMapping.path().length > 0) {
                                methodRoute = putMapping.path()[0];
                            }
                        } else {
                            DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
                            if (deleteMapping != null) {
                                if(deleteMapping.value() != null && deleteMapping.value().length > 0){
                                    methodRoute = deleteMapping.value()[0];
                                } else if (deleteMapping.value() != null && deleteMapping.path().length > 0) {
                                    methodRoute = deleteMapping.path()[0];
                                }
                            } else {
                                PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
                                if (patchMapping != null) {
                                    if(patchMapping.value() != null && patchMapping.value().length > 0){
                                        methodRoute = patchMapping.value()[0];
                                    } else if (patchMapping.value() != null && patchMapping.path().length > 0) {
                                        methodRoute = patchMapping.path()[0];
                                    }
                                }
                            }
                        }
                    }
                }
                if (!StringUtils.isEmpty(classRoute) && !StringUtils.isEmpty(methodRoute)) {
                    String parent = ReplaceRoute.value();
                    if((parent.lastIndexOf("/") != parent.length() - 1 && (methodRoute.indexOf("/") != 0)) || StringUtils.isEmpty(parent)){
                        parent += '/';
                    }
                    if(classRoute.lastIndexOf("/") != classRoute.length() - 1 && (methodRoute.indexOf("/") != 0)){
                        classRoute += '/';
                    }
                    String originalRoute = parent + methodRoute;
                    String redirectRoute = classRoute + methodRoute;
                    if(originalRoute.indexOf("/") != 0){
                        originalRoute = '/' + originalRoute;
                    }
                    if(redirectRoute.indexOf("/") != 0){
                        redirectRoute = '/' + redirectRoute;
                    }
                    routerMap.put(originalRoute, redirectRoute);
                }
            }
        }
        if (routerMap.size() > 0) {
            log.info("扫描路由方法覆盖：" + routerMap.size() + "个");
        }
    }

    public static boolean checkExistCover(String originalRoute) {
        return routerMap.containsKey(originalRoute);
    }

    public static String getRedirectRoute(String originalRoute) {
        return routerMap.get(originalRoute);
    }
}
