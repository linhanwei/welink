package com.welink.biz.common.model;

/**
 * Created by daniel on 14-9-28.
 */
public class PushParam {

    String url = "";

    long tid = -1;

    /**
     * bizType
     */
    long t = -1;

    /**
     * 邻里消息数量
     */
    long c = 0;

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public long getC() {
        return c;
    }

    public void setC(long c) {
        this.c = c;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
