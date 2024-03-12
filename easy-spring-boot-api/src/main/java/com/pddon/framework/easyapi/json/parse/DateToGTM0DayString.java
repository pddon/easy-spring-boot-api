/**  
* Title DateToGTM0DayString.java  
* Description  Date转GTM0日期字符串
* @author danyuan
* @date May 7, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.json.parse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DateToGTM0DayString extends JsonSerializer<Date> {
	public static final String PARTNER = "yyyy-MM-dd";
	public static final String TIMEZONE = "GTM";
	public static SimpleDateFormat formatter;
	static{
		formatter = new SimpleDateFormat(PARTNER);
		formatter.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
	}
	/**
	 * @param value
	 * @param gen
	 * @param serializers
	 * @throws IOException
	 * @author danyuan
	 */
	@Override
	public void serialize(Date value, JsonGenerator gen,
			SerializerProvider serializers) throws IOException {		
        String formattedDate = formatter.format(value);
        gen.writeString(formattedDate);
	}

}
