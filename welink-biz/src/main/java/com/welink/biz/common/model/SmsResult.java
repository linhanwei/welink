package com.welink.biz.common.model;

/**
 * Created by daniel on 14-9-17.
 */
public class SmsResult {
    int count;
    int fee;
    Long sid;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public Long getSid() {
        return sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }
}
