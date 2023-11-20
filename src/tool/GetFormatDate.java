package tool;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 获取格式化日期
 */
public class GetFormatDate {
    public static String getFormatDate(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return "【"+simpleDateFormat.format(date)+"】";
    }
}
