package com.welink.web.common.util;

import com.welink.biz.util.TimeUtils;
import com.welink.commons.commons.BizConstants;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 14-12-4.
 */
public class CookieCheckUtils {

    public static String getCookie(HttpServletRequest request, String key) {
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                if (key.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    return value;
                }
            }
        }
        return null;
    }

    public List<Cookie> addCookie(String sid) {
        Cookie cookieU = new Cookie(BizConstants.JSESSION_ID, sid);
        cookieU.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
        cookieU.setPath("/");
        List<Cookie> list = new ArrayList<Cookie>();
        list.add(cookieU);
        return list;
    }
}
