package com.welink.commons.tacker;

import com.google.common.base.Preconditions;
import com.welink.commons.utils.PhenixUserHander;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by saarixx on 29/11/14.
 */
public class EventTracker {

    static Logger logger = LoggerFactory.getLogger("user-event-track");

    public final static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public final static String DEFAULT = "anonymous";

    public static void track(String mobile, String category, String action) {
        track(mobile, category, action, null, null);
    }

    public static void track(String mobile, String category, String action, String key, Long num) {
        mobile = StringUtils.isNotBlank(mobile) ? mobile : DEFAULT;
        category = StringUtils.isNotBlank(category) ? category : DEFAULT;
        action = StringUtils.isNotBlank(action) ? action : DEFAULT;
        String time = new DateTime().toString(formatter);
        key = StringUtils.isNotBlank(key) ? key : DEFAULT;
        String value = num != null ? String.valueOf(num) : "1";
        logger.info(String.format("[%s] [%s] [%s] [%s] [%s] [%s]", time, mobile, category, action, key, value));
    }
}
