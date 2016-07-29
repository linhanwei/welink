package com.welink.commons;

import com.google.common.base.Preconditions;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by saarixx on 13/12/14.
 */
@Service
public class Env {

    @Resource
    private Environment environment;

    private boolean online;

    public boolean isProd() {
        return online;
    }

    public boolean isDev() {
        return !online;
    }

    public boolean isTest() {
        return !online;
    }

    @PostConstruct
    public void init() {
        String[] activeProfiles = environment.getActiveProfiles();
        Preconditions.checkArgument(activeProfiles != null && activeProfiles.length > 0);
        online = "prod".equals(activeProfiles[0]);

    }
}

