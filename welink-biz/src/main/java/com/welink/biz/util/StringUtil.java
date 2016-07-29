package com.welink.biz.util;

import com.welink.biz.common.security.Base64Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 14-10-13.
 */
public class StringUtil {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(StringUtil.class);

    public String escapeHtml(String input) {
        return StringEscapeUtils.escapeHtml4(input);
    }

    public String base64(String input) {
        try {
            return Base64Utils.encode(input.getBytes("utf-8"));
        } catch (Exception e) {
            log.error("base 64 error, input:" + input);
        }
        return "";
    }

    public String fetchHtmlContent(String content) {
        if (StringUtils.isNotBlank(content)) {
            String tmp = StringUtils.substring(content, 0, 120);
            String s = Jsoup.parse(content).text();
            if (StringUtils.isNotBlank(s)) {
                return StringUtils.substring(s, 0, 50);
            }
        }
        return "";
    }

    public String anonymousMobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return "";
        }
        if (mobile.length() < 11) {
            return "";
        }
        String pre = StringUtils.substring(mobile, 0, 3);
        String end = StringUtils.substring(mobile, mobile.length() - 4, mobile.length());

        return pre + "****" + end;
    }

    public String escapeJson(String input) {
        return JSONObject.escape(input);
    }

    public static String escapeJso1n(String input) {
        return JSONObject.escape(input);
    }

    public static void main(String[] args) {
        // System.out.println(escapeJson(base64("您预订的哇哈哈已经派送")));
        String s = "18605816526";
        String url = "http://karsatest.b0.upaiyun.com/1/LTE=/SVRFTS1QVUJMSVNI/MA==/20141127/vNTT-0-1417076831052.jpg";
        System.out.println(escapeJso1n(url));
//        s = "这里是通知的内容";
//        String ss = "这里是同志的\\呢\"荣";
//        System.out.println(JSONObject.escape(ss));
    }
}
