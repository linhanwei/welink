package com.welink.biz.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 15-3-20.
 */
public class GaoDeResult {
    String status;
    String info;
    String count;
    List<Geocodes> geocodes = new ArrayList<>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public List<Geocodes> getGeocodes() {
        return geocodes;
    }

    public void setGeocodes(List<Geocodes> geocodes) {
        this.geocodes = geocodes;
    }
}
