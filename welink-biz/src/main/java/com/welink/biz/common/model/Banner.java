package com.welink.biz.common.model;

/**
 * Created by daniel on 15-1-14.
 */
public class Banner {
    String ur;
    int type;
    int act;
    String redirectUrl;

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getUr() {
        return ur;
    }

    public void setUr(String ur) {
        this.ur = ur;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAct() {
        return act;
    }

    public void setAct(int act) {
        this.act = act;
    }
}
