package com.welink.biz.resources;

import java.text.MessageFormat;

/**
 * Created by daniel on 14-10-17.
 */
public class MsgTemplate {

    //您已欠物业费[2014年07－2014年12月，共计6个月，1800.00]，已缴纳成功
    public static String MUST_PMF_PAY_SUCCESS = "您已欠物业费[{0}－{1}，共计{2}个月，{3}]，已缴纳成功";

    //您预缴物业费[2014年07－2014年12月，共计6个月，1800.00]，已缴纳成功
    public static String PRE_PMF_PAY_SUCCESS = "您预缴物业费[{0}－{1}，共计{2}个月，{3}]，已缴纳成功";

    public static void main(String[] args) {
        String s = MessageFormat.format(MUST_PMF_PAY_SUCCESS, "2014年2月", "2014年8月", "7", "1900.00");
        System.out.println(s);
    }
}
