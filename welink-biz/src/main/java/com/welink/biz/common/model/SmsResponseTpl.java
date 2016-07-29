package com.welink.biz.common.model;

/**
 * Created by daniel on 14-9-17.
 */
public class SmsResponseTpl {
    int code;
    String msg;
    SmsResult result = new SmsResult();

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public SmsResult getResult() {
        return result;
    }

    public void setResult(SmsResult result) {
        this.result = result;
    }
}
