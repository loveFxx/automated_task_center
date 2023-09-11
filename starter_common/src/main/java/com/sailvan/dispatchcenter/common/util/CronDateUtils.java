package com.sailvan.dispatchcenter.common.util;

import lombok.SneakyThrows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 该类提供Quartz的cron表达式与Date之间的转换
 * @author mh
 * @date 2021
 */
public class CronDateUtils {
    private static final String CRON_DATE_FORMAT = "ss mm HH dd MM ? yyyy";

    /***
     *
     * @param time 时间
     * @return  cron类型的日期
     */
    @SneakyThrows
    public static String getCron(final String time){
        Date  date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        SimpleDateFormat sdf = new SimpleDateFormat(CRON_DATE_FORMAT);
        String formatTimeStr = "";
        if (date != null) {
            formatTimeStr = sdf.format(date);
        }
        return formatTimeStr;
    }

    /***
     *
     * @param cron Quartz cron的类型的日期
     * @return  Date日期
     */

    public static Date getDate(final String cron) {
        if(cron == null) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(CRON_DATE_FORMAT);
        Date date = null;
        try {
            date = sdf.parse(cron);
        } catch (ParseException e) {
            // 此处缺少异常处理,自己根据需要添加
            return null;
        }
        return date;
    }
}