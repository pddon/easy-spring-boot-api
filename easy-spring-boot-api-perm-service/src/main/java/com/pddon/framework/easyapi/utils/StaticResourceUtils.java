package com.pddon.framework.easyapi.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName: StaticResourceUtils
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-19 22:25
 * @Addr: https://pddon.cn
 */
public class StaticResourceUtils {
    // 定义一组静态资源的路径模式
    private static final String[] STATIC_RESOURCE_PREFIX_PATTERNS = new String[] {
            "/static/", "/html/", "/vue/", "/css/", "/js/", "/images/", "/img/", "/fonts/", "/assets/"
    };

    private static final String[] STATIC_RESOURCE_SUFFIX_PATTERNS = new String[] {
            ".html", ".vue", ".css", ".js", ".jpg", ".icon", ".jpeg", ".png", ".svg", ".gif"
    };

    public static boolean isStaticResourceRequest(HttpServletRequest request) {
        String uri = request.getRequestURI(); // 获取请求的URI
        // 遍历所有静态资源的路径模式，检查URI是否匹配
        for (String pattern : STATIC_RESOURCE_PREFIX_PATTERNS) {
            if (uri.contains(pattern)) {
                return true;
            }
        }
        for (String pattern : STATIC_RESOURCE_SUFFIX_PATTERNS) {
            if (uri.endsWith(pattern)) {
                return true;
            }
        }
        return false; // 如果不是以上路径的请求，返回false
    }
}
