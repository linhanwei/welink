package com.welink.commons.tacker;

import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.GoogleAnalyticsResponse;
import com.welink.commons.Env;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by saarixx on 7/1/15.
 */
@Service
public class GoogleAnalyticsTracker {

    static Logger logger = LoggerFactory.getLogger(GoogleAnalyticsTracker.class);

    @Resource
    private Env env;

    private GoogleAnalytics googleAnalytics;

    public void track(String category, String action, String key, Integer num) {

        checkArgument(StringUtils.isNotBlank(category));
        checkArgument(StringUtils.isNotBlank(action));
        checkArgument(StringUtils.isNotBlank(key));
        checkArgument(num > 0);

        GoogleAnalyticsResponse response = googleAnalytics.post(new EventHit(category, action, key, num));

        logger.info("google analytics response --> {}", response.toString());
    }

    @PostConstruct
    public void init() {

        if (env.isProd()) {
            googleAnalytics = new GoogleAnalytics("UA-58312531-1");
        } else {
            googleAnalytics = new GoogleAnalytics("UA-58339149-1");
        }

        googleAnalytics.getConfig().setGatherStats(true);
        googleAnalytics.getConfig().setValidate(true);

    }
}
