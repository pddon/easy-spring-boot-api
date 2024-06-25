/**  
* Title HttpHelper.java  
* Description  
* @author danyuan
* @date Jul 30, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

@Slf4j
public class HttpHelper {

    /**
     * 获取请求Body
     *
     * @param request
     * @return
     */
    public static String getBodyString(ServletRequest request) {
        if(request.getContentType() != null && request.getContentType().contains("multipart")){
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(IOUtils.getThrowableInfo(e));
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                	log.error(e.getMessage());
                    log.error(IOUtils.getThrowableInfo(e));
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                	log.error(e.getMessage());
                    log.error(IOUtils.getThrowableInfo(e));
                }
            }
        }
        return sb.toString();
    }

    public static String getJsonBodyStringParam(ServletRequest request, String paramName){
        if(request.getContentType() != null && request.getContentType().contains("multipart")){
            return null;
        }
        String body = getBodyString(request);
        if(StringUtils.isEmpty(body)){
            return null;
        }
        try{
            JSONObject json = JSONUtil.parseObj(body);
            return json.getStr(paramName);
        }catch (Exception e){
            return null;
        }
    }

    public static String getParam(ServletRequest request, String paramName){
        return getParams(request, paramName)[0];
    }

    public static String[] getParams(ServletRequest request, String... paramNames){
        if(request.getContentType() != null && request.getContentType().contains("multipart")){
            return new String[]{null, null};
        }
        String[] values = new String[paramNames.length];
        HttpServletRequest servletRequest = WebUtils.toHttp(request);
        String method = ((HttpServletRequest) request).getMethod();
        JSONObject json = null;
        if(!RequestMethod.GET.name().equals(method) &&
                !RequestMethod.HEAD.name().equals(method) &&
                !RequestMethod.TRACE.name().equals(method)){
            json = JSONUtil.parseObj(HttpHelper.getBodyString(request));
        }
        for(int i = 0; i < paramNames.length; i++){
            values[i] = null;
            String key = SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(paramNames[i]);
            //从query参数中获取
            try {
                values[i] = ServletRequestUtils.getStringParameter(request, key);
                if(values[i] != null){
                    continue;
                }
            } catch (ServletRequestBindingException e) {
                //
            }
            //从header中获取
            values[i] = servletRequest.getHeader(key);
            if(values[i] != null){
                continue;
            }
            //从body中获取
            if(json != null && json.containsKey(paramNames[i])){
                values[i] = json.get(paramNames[i]).toString();
            }
        }
        return values;
    }
}