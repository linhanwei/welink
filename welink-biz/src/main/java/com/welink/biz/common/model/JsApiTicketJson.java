package com.welink.biz.common.model;

import com.daniel.weixin.mp.util.json.WxMpGsonBuilder;

/**
 * Created by XUTIAN on 2015/1/19.
 */
public class JsApiTicketJson {

    private String errCode;

    private String errorMsg;

    private String ticket;

    private int expiresIn = -1;

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public static JsApiTicketJson fromJson(String json) {
        return WxMpGsonBuilder.create().fromJson(json, JsApiTicketJson.class);
    }

    @Override
    public String toString() {
        return "JsApiTicketJson{" +
                "errCode='" + errCode + '\'' +
                ", expiresIn=" + expiresIn +
                ", errorMsg='" + errorMsg + '\'' +
                ", ticket='" + ticket + '\'' +
                '}';
    }
}
