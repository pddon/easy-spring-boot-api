/**  
* Title BufferedServletInputStream.java  
* Description  
* @author danyuan
* @date Jul 31, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.utils.request;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class BufferedServletInputStream extends ServletInputStream {
    private ByteArrayInputStream inputStream;

    public BufferedServletInputStream(byte[] buffer) {
        this.inputStream = new ByteArrayInputStream(buffer);
    }

    @Override
    public int available() {
        return inputStream.available();
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public boolean isFinished() {
        return inputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
        throw new RuntimeException("Not implemented");
    }
}