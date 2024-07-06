package com.pddon.framework.easyapi.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: StringFormatUtil
 * @Description: 字符串格式化工具类
 * @Author: Allen
 * @Date: 2024-06-06 20:15
 * @Addr: https://pddon.cn
 */
public class StringFormatUtil {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%s");

    public static String formatParams(String template, Object... replacements) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        int i = 0;
        while (matcher.find()) {
            if (i < replacements.length) {
                String param = replacements[i] != null ? replacements[i].toString() : "[参数缺失]";
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(param));
                i++;
            } else {
                // 如果替换的数组不足以替换所有的占位符，可以选择抛出异常或者以某种方式处理
                //throw new IllegalArgumentException("Not enough replacements for the placeholders.");
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static void main(String[] args) {
        String template = "Hello, %s! How is %s today?";
        String result = formatParams(template, "Alice", "Monday");
        System.out.println(result);
    }
}
