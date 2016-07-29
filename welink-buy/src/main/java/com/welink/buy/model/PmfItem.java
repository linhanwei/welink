package com.welink.buy.model;

import java.io.Serializable;

/**
 * Created by daniel on 14-10-5.
 */
public class PmfItem implements Serializable {

    int months;

    long startTime;

    long price;

    long unitPrice;

    public Long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
