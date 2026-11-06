/**  
* Title BodyReaderHttpServletRequestWrapper.java  
* Description  
* @author danyuan
* @date Jul 30, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.utils.request;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = readBodyBytes(request);
    }

    /**
     * 直接读取原始字节，避免旧实现"读取为String→按UTF-8重新编码为byte[]"的两次额外分配。
     * 读取后的字节同时用于getInputStream与getReader，保证重复读取可用。
     */
    private static byte[] readBodyBytes(HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        InputStream inputStream = request.getInputStream();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(
                    contentLength > 0 ? Math.min(contentLength, 8192) : 32);
            byte[] chunk = new byte[8192];
            int read;
            while ((read = inputStream.read(chunk)) != -1) {
                out.write(chunk, 0, read);
            }
            return out.toByteArray();
        } finally {
            inputStream.close();
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new BufferedServletInputStream(this.body);
    }
}