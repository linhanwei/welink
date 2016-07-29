package com.welink.biz.common.model;

/**
 * Created by daniel on 14-11-5.
 */
public class PushNoAlert {

    String action;

    String badge = "Increment";

    PushParam p;

    public PushParam getP() {
        return p;
    }

    public void setP(PushParam p) {
        this.p = p;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }
}
