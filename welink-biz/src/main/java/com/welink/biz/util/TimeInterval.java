package com.welink.biz.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by daniel on 14-10-16.
 */
public class TimeInterval {
    /**
     * Description：<code> 处理微博过去时间显示效果 </code>
     *
     * @param createAt Data 类型
     * @return { String }
     * @throws
     */
    public static String getInterval(Date createAt) {
        //定义最终返回的结果字符串。
        String interval = null;

        long millisecond = new Date().getTime() - createAt.getTime();

        long second = millisecond / 1000;

        if (second <= 0) {
            second = 0;
        }

        if (second == 0) {
            interval = "刚刚";
        } else if (second < 30) {
            interval = second + "秒以前";
        } else if (second >= 30 && second < 60) {
            interval = "半分钟前";
        } else if (second >= 60 && second < 60 * 60) {
            long minute = second / 60;
            interval = minute + "分钟前";
        } else if (second >= 60 * 60 && second < 60 * 60 * 24) {
            long hour = (second / 60) / 60;
            if (hour <= 3) {
                interval = hour + "小时前";
            } else {
                interval = "今天" + getFormatTime(createAt, "hh:mm");
                interval = hour + "小时前";
            }
        } else if (second >= 60 * 60 * 24 && second <= 60 * 60 * 24 * 2) {
            interval = "昨天" + getFormatTime(createAt, "hh:mm");
        } else if (second >= 60 * 60 * 24 * 2 && second <= 60 * 60 * 24 * 30) {
            long day = ((second / 60) / 60) / 24;
            interval = day + "天前";
        } else if (second >= 60 * 60 * 24 * 30) {
            interval = "1个月前";//getFormatTime(createAt, "MM-dd hh:mm");
        } else if (second >= 60 * 60 * 24 * 365) {
            interval = getFormatTime(createAt, "YYYY-MM-dd hh:mm");
        } else {
            interval = "0";
        }
        // 最后返回处理后的结果。
        return interval;
    }

    /**
     * Description：<code> 返回指定格式的Date </code>
     * String
     *
     * @param date
     * @param Sdf
     * @return
     * @throws
     */
    public static String getFormatTime(Date date, String Sdf) {
        return (new SimpleDateFormat(Sdf)).format(date);
    }

    //-----------------------------非静态方法--------------------------------//

    /**
     * Description：<code> 处理微博过去时间显示效果 </code>
     *
     * @param createAt Data 类型
     * @return { String }
     * @throws
     */
    public String getIntervaln(Date createAt) {
        //定义最终返回的结果字符串。
        String interval = null;

        long millisecond = new Date().getTime() - createAt.getTime();

        long second = millisecond / 1000;

        if (second <= 0) {
            second = 0;
        }

        if (second == 0) {
            interval = "刚刚";
        } else if (second < 30) {
            interval = second + "秒以前";
        } else if (second >= 30 && second < 60) {
            interval = "半分钟前";
        } else if (second >= 60 && second < 60 * 60) {
            long minute = second / 60;
            interval = minute + "分钟前";
        } else if (second >= 60 * 60 && second < 60 * 60 * 24) {
            long hour = (second / 60) / 60;
            if (hour <= 3) {
                interval = hour + "小时前";
            } else {
                interval = "今天" + getFormatTimen(createAt, "hh:mm");
                interval = hour + "小时前";
            }
        } else if (second >= 60 * 60 * 24 && second <= 60 * 60 * 24 * 2) {
            interval = "昨天" + getFormatTimen(createAt, "hh:mm");
        } else if (second >= 60 * 60 * 24 * 2 && second <= 60 * 60 * 24 * 30) {
            long day = ((second / 60) / 60) / 24;
            interval = day + "天前";
        } else if (second >= 60 * 60 * 24 * 30) {
            interval = "1个月前";
        } else if (second >= 60 * 60 * 24 * 365) {
            interval = getFormatTimen(createAt, "YYYY-MM-dd hh:mm");
        } else {
            interval = "0";
        }
        // 最后返回处理后的结果。
        return interval;
    }

    /**
     * Description：<code> 返回指定格式的Date </code>
     * String
     *
     * @param date
     * @param Sdf
     * @return
     * @throws
     */
    public String getFormatTimen(Date date, String Sdf) {
        return (new SimpleDateFormat(Sdf)).format(date);
    }


    public static void main(String[] args) {
        System.out.println(getInterval(new Date()));
        String s = "2014-09-17 01:30:34";
        Date d = TimeUtils.str2DateTime(s);
        System.out.println(getInterval(d));
    }
}
