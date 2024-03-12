package com.pddon.framework.easyapi.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class IOUtils {
	public static String getThrowableInfo(Throwable throwable) {
		try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
            PrintStream printStream = new PrintStream(outputStream);
            throwable.printStackTrace(printStream);
            return outputStream.toString();
        }catch (Exception e){
            return e.getMessage();
        }
	}
	
	public static String getThrowableInfoLess(Throwable throwable) {
		
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(256);
        PrintStream printStream = new PrintStream(outputStream);
        throwable.printStackTrace(printStream);
        return outputStream.toString();
	}
}
