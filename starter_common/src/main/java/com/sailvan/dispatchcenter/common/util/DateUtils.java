package com.sailvan.dispatchcenter.common.util;

import com.sailvan.dispatchcenter.common.constant.Constant;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Title: DateUtils
 * @author mh
 * @date 2021
 */
public class DateUtils {

    public final static String DATE_FORMAT_HOUR = "yyyy-MM-dd HH:mm:ss";
    public final static String DATE_FORMAT_DAY = "yyyy-MM-dd 00:00:00";

    /**
     *
     * 功能描述:
     *
     * @param: 获取当前系统时间 yyyy-MM-dd HH:mm:ss
     * @return:
     */
    public static String getCurrentDate(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(System.currentTimeMillis());
        return date;
    }

    public static String getCurrentDateStart(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String date = df.format(System.currentTimeMillis());
        return date;
    }

    public static String getAfterStart(String time, int num) throws Exception {
        if (time == null || "".equals(time)) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        Calendar ca = Calendar.getInstance();
        ca.setTime(format.parse(time));

        // num为增加的天数，可以改变的
        ca.add(Calendar.DATE, num);
        return format.format(ca.getTime());
    }


    /**
     *
     * 功能描述: 
     *
     * @param: date类 获取当前系统时间 yyyy-MM-dd HH:mm:ss
     */
    public static Date getCurrentDateToDate () {
        DateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        String date = df.format(System.currentTimeMillis());
        Date d = null;
        try {
            d = df.parse( date.toString( ) );
        } catch ( ParseException e ) {
            e.printStackTrace( );
        }
        return d;
    }

    /**
     * 增加时间单位：天
     * @param day
     * @return
     */
    public static String getCurrentAddDay(int day) {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, day);
        return sdf.format(cal.getTime());
    }

    /**
     * 增加时间单位：分钟
     * @param minute
     * @return
     */
    public static String getCurrentAddMin(int minute) {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, minute);
        return sdf.format(cal.getTime());
    }

    /**
     * 获取当前时间
     * @return
     */
    public static String getNowDateString (  ) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd");
        return sdf.format( d );
    }

    /**
     * 把Date转为String
     * @param date
     * @param format
     * @return
     */
    public static String getFormatTime(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 增加时间单位：天
     * @param day
     * @return
     */
    public static Date addDay(int day,Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, day);
        return cal.getTime();
    }

    /**
     * 获取当月几号日期
     * @param day
     * @return
     */
    public static String getDayOfMonth(int day){
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return sdf.format(calendar.getTime());
    }

    /**
     * 当前时间减几天
     * @param day
     * @return
     */
    public static Date minusDay(int day,Date date){
        return addDay(-day, date);
    }

    /**
     * 获取上月初
     * @return
     */
    public static String getLastMonthDayOne() {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.MONTH, -1);
        calendar1.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(calendar1.getTime());
    }

    /**
     * 获取上月末
     * @return
     */
    public static String getLastMonthLastDay() {
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.DAY_OF_MONTH, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(calendar2.getTime());
    }

    /**
     * 获取上月
     * @return
     */
    public static String getLastMonth() {
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.DAY_OF_MONTH, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        return sdf.format(calendar2.getTime());
    }


    public static String getDate(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String banPeriod = df.format(new Date());
        return banPeriod;
    }

    /**
     *  几天后 n=0是当天
     * @param num
     * @return
     * @throws Exception
     */
    public static String getAfterDays(int num) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return getAfter(format.format(new Date()), num);
    }

    public static String getAfterDays(int num,String formats) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat(formats);
        return getAfterDay(format.format(new Date()), num, formats);
    }

    public static String getAfterDay(String time, int num,String formats) throws Exception {
        if (time == null || "".equals(time)) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(formats);
        Calendar ca = Calendar.getInstance();
        ca.setTime(format.parse(time));

        // num为增加的天数，可以改变的
        ca.add(Calendar.DATE, num);
        return format.format(ca.getTime());
    }

    public static String getAfter(String time, int num) throws Exception {
        if (time == null || "".equals(time)) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar ca = Calendar.getInstance();
        ca.setTime(format.parse(time));

        // num为增加的天数，可以改变的
        ca.add(Calendar.DATE, num);
        return format.format(ca.getTime());
    }

    public static long getAfterDaysTimeMillis(int num) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String after = getAfter(format.format(new Date()), num);
        Date dateAfter = format.parse(after);
        return dateAfter.getTime();
    }


    public static int getDaysOfCurrentMonth() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String format = sdf.format(new Date());
        Date date = sdf.parse(format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static String getTokenValidityTime(){
        Date date = new Date(System.currentTimeMillis() + Constant.TOKEN_VALIDITY_TIME * 60 * 1000);
        SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return df.format(date);
    }

    public static String convertISOTime(String time) throws ParseException {
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
        format.setTimeZone(tz);
        return format.format(format2.parse(time));
    }

    /**
     * 计算两个时间差，返回为秒
     * @param time1
     * @param time2
     * @return
     */
    public static long calTime(String time1, String time2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long second = 0L;
        try {
            Date d1 = df.parse(time1);
            Date d2 = df.parse(time2);
            long diff = d1.getTime() - d2.getTime();
            second = diff / (1000);
        } catch (ParseException e) {
            System.out.println("抱歉，时间日期解析出错。");
        }
        return second;
    }

    public static Date convertDateFromISO(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = sdf.parse(time);
        return date;
    }

    public static Date convertDate(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(time);
        return date;
    }

    public static String convertTimestampToDate(BigInteger timestamp){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
    }

    public static String getHourBeforeDate(int hour){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(calendar.HOUR_OF_DAY, -hour);
        return sdf.format(calendar.getTime());
    }

    public static String getDayBeforeDate(int hour){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(calendar.DAY_OF_MONTH, -hour);
        return sdf.format(calendar.getTime());
    }

    public static int getCurrentHour(){
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String timeShort = df.format(System.currentTimeMillis());
        StringTokenizer st = new StringTokenizer(timeShort, ":");
        List<String> inTime = new ArrayList<String>();
        while (st.hasMoreElements()) {
            inTime.add(st.nextToken());
        }
        return Integer.parseInt(inTime.get(0));
    }



    /**
     * 根据日期算出当天和跨天的起始时间
     *
     * @param date 2021-11-27
     * @return Map{"oneDayStart":"2021-11-27 00:00:00","oneDayEnd":"2021-11-27 23:59:59"...}
     * @throws ParseException
     */
    public static Map<String, String> getStartEndOfDay(String date) throws ParseException {


        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
        Date oneDayDate =sdf.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(oneDayDate);
        cal.add(Calendar.DATE, 1);

        String secondDay = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

        String oneDayStart=date+" 00:00:00";
        String oneDayEnd=date+" 23:59:59";
        String secondDayStart=secondDay+" 00:00:00";
        String secondDayEnd=secondDay+" 23:59:59";

        Map<String, String> res = new HashMap<>();
        res.put("oneDayStart",oneDayStart);
        res.put("oneDayEnd",oneDayEnd);
        res.put("secondDayStart",secondDayStart);
        res.put("secondDayEnd",secondDayEnd);

        return res;

    }

    /**
     * 当天0点时间戳
     * @return
     */
    public static long getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime().getTime();
    }

    /**
     * 获取指定时间前几小时
     * @param date 指定时间
     * @param num 小时
     * @return 时间
     * @throws ParseException
     */
    public static String getHourBeforeDate(String date,int num) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(simpleDateFormat.parse(date));
        calendar.add(Calendar.HOUR_OF_DAY, -num);
        return simpleDateFormat.format(calendar.getTime());
    }
}
