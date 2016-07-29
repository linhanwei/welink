package com.welink.buy.utils;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间窗口程序
 * <p/>
 * Created by saarixx on 17/9/14.
 */
public final class TimeUtil {

    static Logger logger = LoggerFactory.getLogger(TimeUtil.class);

    public static long getTimeBeforeOrAfterDays(Date curDate, int iDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.DAY_OF_MONTH, iDate);
        return cal.getTime().getTime();
    }

    public static Date getDateBeforeOrAfterDays(Date curDate, int iDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.DAY_OF_MONTH, iDate);
        return cal.getTime();
    }

    public static long getDateAfterMonth(Date curDate, int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.MONTH, number);
        return cal.getTime().getTime();
    }

    public static long getDateAfterHour(Date curDate, int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.HOUR, number);
        return cal.getTime().getTime();
    }

    public static Date getDateAfterHourAsDate(Date curDate, int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.HOUR, number);
        return cal.getTime();
    }

    public static Date getDateAfterMonthDate(Date curDate, int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.MONTH, number);
        return cal.getTime();
    }

    public static Date getDateAfterYearDate(Date curDate, int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.YEAR, number);
        return cal.getTime();
    }

    /**
     * 系统某个时间过N个月后的时间
     *
     * @param nextStep 月份偏移量
     * @return
     */
    public static Date getNextMonth(Date base, int nextStep) {
        Calendar c = Calendar.getInstance();
        c.setTime(base);
        int m = c.get(Calendar.MONTH);
        c.set(Calendar.MONTH, m + nextStep);
        return c.getTime();
    }

    /**
     * 获取给定时间所在的年份
     *
     * @param date 时间
     * @return 时间所在的年份
     */
    public static int getYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }

    /**
     * 获取给定时间所在的月份
     *
     * @param date 时间
     * @return 时间所在的月份
     */
    public static int getMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MONTH);
    }

    //获取某个日期N个月后的最后一天
    public static Date getLastDayOffSet(Date base, int offMonths) {
        Date offsetDate = getNextMonth(base, offMonths);
        Calendar cal = Calendar.getInstance();
        cal.setTime(offsetDate);
        cal.set(Calendar.YEAR, getYear(offsetDate));// 年
        cal.set(Calendar.MONTH, getMonth(offsetDate));// 月，因为Calendar里的月是从0开始，所以要减1
        cal.set(Calendar.DATE, 1);// 日，设为一号
        cal.add(Calendar.MONTH, 1);// 月份加一，得到下个月的一号
        cal.add(Calendar.DATE, -1);// 下一个月减一为本月最后一天
        return cal.getTime();// 获得月末是几号
    }

    //获取某个日期N个月后的最后一天
    public static Date getLastDayAndSecondOffSet(Date base, int offMonths) {
        Date offsetDate = getNextMonth(base, offMonths);
        Calendar cal = Calendar.getInstance();
        cal.setTime(offsetDate);
        cal.set(Calendar.YEAR, getYear(offsetDate));// 年
        cal.set(Calendar.MONTH, getMonth(offsetDate));// 月，因为Calendar里的月是从0开始，所以要减1
        cal.set(Calendar.DATE, 1);// 日，设为一号
        cal.add(Calendar.MONTH, 1);// 月份加一，得到下个月的一号
        cal.add(Calendar.DATE, -1);// 下一个月减一为本月最后一天
        cal.add(Calendar.HOUR, 23);
        cal.add(Calendar.MINUTE, 59);
        cal.add(Calendar.SECOND, 59);
        return cal.getTime();// 获得月末是几号
    }

    /**
     * 某个时间点的下个月的第一天
     *
     * @param day
     * @return
     */
    public static Date firstDayInNextMonth(Date day) {
        Calendar c = Calendar.getInstance();
        c.setTime(day);
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    /**
     * 某个时间点的下个月的第一天的开始时间
     *
     * @param day
     * @return
     */
    public static Date firstDayInNextMonthStart(Date day) {
        Calendar c = Calendar.getInstance();
        c.setTime(day);
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.HOUR, 0);
        c.add(Calendar.MINUTE, 0);
        c.add(Calendar.SECOND, 0);
        return c.getTime();
    }

    public static String getStrDateBeforeOrAfter(Date curDate, int iDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.DAY_OF_MONTH, iDate);
        return _date2Str(cal.getTime());
    }

    public static String _date2Str(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static String date2StrMonthEnd(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
        return sdf.format(date);
    }

    public static Date str2DateTime(String str) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            logger.error("transform date error. str:" + str);
        }
        return date;
    }

    public static void main(String[] args) {
        String s = "2013-01-28 15:30:22";
        System.out.println(firstDayInNextMonth(str2DateTime(s)));
        System.out.print("===" + getLastDayOffSet(str2DateTime(s), 2));
    }


    public static Date plusNDay(Date time, int day) {
        return new DateTime(time.getTime()).plusDays(day).toDate();
    }

    public static Date middleNight() {
        return new DateTime().plusDays(1).withTimeAtStartOfDay().toDate();
    }
}
