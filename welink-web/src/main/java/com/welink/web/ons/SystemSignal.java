package com.welink.web.ons;

import com.google.common.collect.Maps;

import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by saarixx on 13/12/14.
 */
public class SystemSignal {

    private String code;

    private Map<String, String> params = Maps.newHashMap();

    private Date dateCreated = new Date();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
