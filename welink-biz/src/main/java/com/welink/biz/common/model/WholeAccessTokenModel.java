package com.welink.biz.common.model;

/**
 * Created by daniel on 14-12-26.
 */
public class WholeAccessTokenModel {

    public String access_token;

    public int expires_in;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }
}
