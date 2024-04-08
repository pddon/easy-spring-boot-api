/**  
* Title CustomJackDateFormat.java  
* Description  
* @author danyuan
* @date May 22, 2019
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.json.parse;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class CustomJackDateFormat extends DateFormat {
    /** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
    private DateFormat dateFormat;

    public CustomJackDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
        // 默认跟随服务器时区        
        dateFormat.setTimeZone(Calendar.getInstance().getTimeZone());
        log.info("自定义 jackson时间 转化格式初始化");
    }


    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return dateFormat.format(date, toAppendTo, fieldPosition);

    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        Date date = null;
        try {
            // 先按秒来解析
            date = DateToGTM0SecondString.formatter.parse(source, pos);
        } catch (Exception e) {
        	try {
                // 再按天来解析
                date = DateToGTM0DayString.formatter.parse(source, pos);
            } catch (Exception e1) {
                // 不行，那就按原先的规则吧
                date = dateFormat.parse(source, pos);
            }
        }
        return date;
    }

    // 这里装饰clone方法的原因是因为clone方法在jackson中也有用到
    @Override
    public Object clone() {
        Object format = dateFormat.clone();
        return new CustomJackDateFormat((DateFormat) format);
    }
}
