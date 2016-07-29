package com.welink.web.common.util;

import com.welink.biz.util.TimeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class ParameterUtil {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ParameterUtil.class);

    public static String getParameter(HttpServletRequest request, String parameterName) throws UnsupportedEncodingException {

        String result = request.getParameter(parameterName);
        if (StringUtils.isNotBlank(result)) {
            return result;
        }

        return "";
    }

    public static long getParameterAslong(String parameterName, long defaultValue) {

        ServletRequest request = ServletActionContext.getRequest();

        String result = request.getParameter(parameterName);

        long l = -1l;


        if (StringUtils.isNotBlank(result)) {
            try {
                return Long.valueOf(result);
            } catch (Exception e) {
                log.error("get long value paramteters failed . exp:" + e.getMessage());
            }
        }

        return defaultValue;
    }

    public static long getParameterAslongForSpringMVC(HttpServletRequest request, String parameterName, long defaultValue) {

        String result = request.getParameter(parameterName);

        long l = -1l;


        if (StringUtils.isNotBlank(result)) {
            try {
                return Long.valueOf(result);
            } catch (Exception e) {
                log.error("get long value paramteters failed . exp:" + e.getMessage());
            }
        }

        return defaultValue;
    }

    public static long getParameterAslong(String parameterName) {

        ServletRequest request = ServletActionContext.getRequest();

        String result = request.getParameter(parameterName);

        long l = -1l;


        if (StringUtils.isNotBlank(result)) {
            try {
                return Long.valueOf(result);
            } catch (Exception e) {
                log.error("get long value paramteters failed . exp:" + e.getMessage());
            }
        }

        return l;
    }

    public static int getParameterAsInt(String parameterName, int defaultValue) {

        ServletRequest request = ServletActionContext.getRequest();

        String result = request.getParameter(parameterName);

        int l = -1;


        if (StringUtils.isNotBlank(result)) {
            try {
                return Integer.valueOf(result);
            } catch (Exception e) {
                log.error("get integer value paramteters failed . exp:" + e.getMessage());
            }
        }

        return defaultValue;
    }

    public static Date getParameterAsDateTime(String parameterName) {

        ServletRequest request = ServletActionContext.getRequest();

        String result = request.getParameter(parameterName);

        Date date = new Date();


        if (StringUtils.isNotBlank(result)) {
            try {
                date = TimeUtils.str2DateTime(result);
                return date;
            } catch (Exception e) {
                log.error("get long value paramteters failed . exp:" + e.getMessage());
            }
        }

        return date;
    }

    public static Date getParameterAsDateTimeFromLong(String parameterName) {

        ServletRequest request = ServletActionContext.getRequest();

        String result = request.getParameter(parameterName);

        Long datel = Long.valueOf(result);

        Date date = new Date();

        date = TimeUtils.long2Date(datel);

        return date;
    }

    public static Date getParameterAsDateTimeFromLongForSpringMVC(HttpServletRequest request, String parameterName) {

        String result = request.getParameter(parameterName);

        Long datel = Long.valueOf(result);

        Date date = new Date();

        date = TimeUtils.long2Date(datel);

        return date;
    }

    public static int getParameterAsInt(String parameterName) {

        ServletRequest request = ServletActionContext.getRequest();

        String result = request.getParameter(parameterName);

        int l = -1;


        if (StringUtils.isNotBlank(result)) {
            try {
                return Integer.valueOf(result);
            } catch (Exception e) {
                log.error("get integer value paramteters failed . exp:" + e.getMessage());
            }
        }

        return l;
    }

    public static int getParameterAsIntForSpringMVC(HttpServletRequest request, String parameterName) {

        String result = request.getParameter(parameterName);

        int l = -1;


        if (StringUtils.isNotBlank(result)) {
            try {
                return Integer.valueOf(result);
            } catch (Exception e) {
                log.error("get integer value paramteters failed . exp:" + e.getMessage());
            }
        }

        return l;
    }

    public static int getParameterAsIntForSpringMVC(HttpServletRequest request, String parameterName,int defaultValue) {

        String result = request.getParameter(parameterName);

        int l = defaultValue;

        if (StringUtils.isNotBlank(result)) {
            try {
                return Integer.valueOf(result);
            } catch (Exception e) {
                log.error("get integer value paramteters failed . exp:" + e.getMessage());
            }
        }

        return l;
    }

    public static byte getParameterAsByte(String parameterName) {

        ServletRequest request = ServletActionContext.getRequest();

        String result = request.getParameter(parameterName);

        byte l = -1;


        if (StringUtils.isNotBlank(result)) {
            try {
                return Byte.valueOf(result);
            } catch (Exception e) {
                log.error("get integer value paramteters failed . exp:" + e.getMessage());
            }
        }

        return (byte) -1;
    }

    public static byte getParameterAsByteForSpringMVC(HttpServletRequest request, String parameterName) {

        String result = request.getParameter(parameterName);

        byte l = -1;


        if (StringUtils.isNotBlank(result)) {
            try {
                return Byte.valueOf(result);
            } catch (Exception e) {
                log.error("get integer value paramteters failed . exp:" + e.getMessage());
            }
        }

        return (byte) -1;
    }


    public static String getUserIpAddress() {

        ServletRequest request = ServletActionContext.getRequest();

        String result = request.getRemoteAddr();

        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        return "unknow";
    }

    public static boolean getParameterAsBoolean(String parameterName) {

        ServletRequest request = ServletActionContext.getRequest();

        String result = request.getParameter(parameterName);

        if (StringUtils.isNotBlank(result)) {
            try {
                return Boolean.valueOf(result);
            } catch (Exception e) {
                log.error("get boolean value paramteters failed . exp:" + e.getMessage());
            }
        }

        return false;
    }

    public static boolean getParameterAsBooleanForSpringMVC(HttpServletRequest request, String parameterName) {

        String result = request.getParameter(parameterName);

        if (StringUtils.isNotBlank(result)) {
            try {
                return Boolean.valueOf(result);
            } catch (Exception e) {
                log.error("get boolean value paramteters failed . exp:" + e.getMessage());
            }
        }

        return false;
    }
}
