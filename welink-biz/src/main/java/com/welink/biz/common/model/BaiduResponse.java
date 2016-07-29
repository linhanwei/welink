package com.welink.biz.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 14-10-15.
 */
public class BaiduResponse {
    int status;
    String message;
    int total;
    List<PlaceEn> results = new ArrayList<>();

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<PlaceEn> getResults() {
        return results;
    }

    public void setResults(List<PlaceEn> results) {
        this.results = results;
    }
}
