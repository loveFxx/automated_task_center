package com.sailvan.dispatchcenter.shard.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataFormat {
    public static String getCurrentDayFormat(String day) {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfResult = new SimpleDateFormat( "yyyyMMdd");
        try {
            Date parse = sdf.parse(day);
            String format = sdfResult.format(parse);
            return format;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getCurrentMonthFormat(String day) {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfResult = new SimpleDateFormat( "yyyyMM");
        try {
            Date parse = sdf.parse(day);
            String format = sdfResult.format(parse);
            return format;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getCurrentMonthYear(String day) {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfResult = new SimpleDateFormat( "yyyy");
        try {
            Date parse = sdf.parse(day);
            String format = sdfResult.format(parse);
            return format;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static List<String> getRangeNameList(Date start, Date end) {
        List<String> result = new ArrayList<>();
        Calendar dd = Calendar.getInstance();

        dd.setTime(start);

        while (dd.getTime().before(end)) {
            // 判断是否到结束日期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            String str = sdf.format(dd.getTime());
            result.add(str);
            // 进行当前天加1
            dd.add(Calendar.DAY_OF_MONTH, 1);
        }
        return result;
    }
}
