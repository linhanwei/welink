package com.welink.biz.util;

import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by daniel on 14-10-21.
 */
public class TokenUtil {

    public static String getSalt() {
        String salt = "yj1oiw1aieks4qr4lbqe5an8jv4zspri";
        Date day = new Date();
        String dayStr = TimeUtils.date2StrMini(day);//yyyy-MM-dd

        salt = dayStr + "-" + String.valueOf(getTimesmorning());
        return salt;
    }

    public static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789yonder";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * token生成器 client rp
     *
     * @param salt
     * @return
     */
    public static long genToken(String salt) {

        Date day = new Date();

        String str = TimeUtils.date2StrMini(day);//yyyy-MM-dd HH:mm

        salt = salt + str + "-" + String.valueOf(getTimesmorning());

        return BKDRHash(salt);
    }

    /**
     * 生成摘要
     *
     * @param param
     * @return
     */
    public static long fetchCode(String param, long date) {

        param = param + "-" + date;

        return BKDRHash(param);
    }

    public static long getHashSalt() {
        return BKDRHash(getSalt());
    }

    public static long BKDRHash(String str) {
        long seed = 131313; // 31 131 1313 13131 131313 etc..
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash * seed) + str.charAt(i);
        }

        return hash;
    }

    /**
     * 获取当天0时
     *
     * @return
     */
    public static int getTimesmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 2);
        cal.set(Calendar.SECOND, 26);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis() / 1000);
    }

    /**
     * 未登录校验token
     *
     * @param token
     * @return
     */
    public static boolean checkUnLoginToken(String token) {
        long validToken = genToken("");
        if (StringUtils.equals(token, String.valueOf(validToken))) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        String s = "adfaaaaaaaaaaaaaadfd";
        System.out.println(BKDRHash(s));
        System.out.println(BKDRHash(s));
        System.out.println(getTimesmorning());
        System.out.println(getHashSalt());
    }
}
