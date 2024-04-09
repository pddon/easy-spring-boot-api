package com.pddon.framework.easyapi.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: UrlParseUtil
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-03 16:25
 * @Addr: https://pddon.cn
 */
public class UrlParseUtil {

    public static Map<String, String[]> parseQuery(String queryStr) throws UnsupportedEncodingException {
        Map<String, List<String>> params = new HashMap<>();
        for(String param : queryStr.split("&")){
            String[] arr = param.split("=");
            String key = URLDecoder.decode(arr[0], "UTF-8");
            String value = URLDecoder.decode(arr[1], "UTF-8");
            List<String> values = params.get(key);
            if(values == null){
                values = new ArrayList<>();
                params.put(key, values);
            }
            values.add(value);
        }
        Map<String, String[]> map = new HashMap<>();
        params.forEach((key, values) -> {
            String[] arr = new String[values.size()];
            for(int i = 0; i < values.size(); i++){
                arr[i] = values.get(i);
            }
            map.put(key, arr);
        });
        return map;
    }
}
