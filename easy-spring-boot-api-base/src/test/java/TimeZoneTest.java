import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @ClassName: TimeZoneTest
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-07 17:20
 * @Addr: https://pddon.cn
 */
public class TimeZoneTest {

    @Test
    public void testGetCurrentTimeZone(){
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println(Calendar.getInstance().getTimeZone().getID());
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"));
        System.out.println(Calendar.getInstance().getTimeZone().getID());
    }
}
