/**  
* Title HttpHelper.java  
* Description  
* @author danyuan
* @date Jul 30, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.utils.request;
import javax.servlet.ServletRequest;

import com.pddon.framework.easyapi.utils.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpHelper {

    /**
     * 获取请求Body
     *
     * @param request
     * @return
     */
    public static String getBodyString(ServletRequest request) {
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

}