package com.welink.biz.common.security;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by daniel on 14-12-9.
 */
public class CookieCheckUtils {

    public static String getCookie(HttpServletRequest request, String key) {
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        //System.out.println("cookies: " + cookies);
        if (cookies != null) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                //System.out.println("cookieName: " + cookie.getName());
                if (key.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    //System.out.println("cookieValue: " + cookie.getValue());
                    return value;
                }
            }
        }
        return null;
    }
}
