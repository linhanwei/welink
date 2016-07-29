package com.welink.web.common.model;

/**
 * Created by daniel on 14-9-16.
 */
public class ResponseResult {

    int status = 0;

    String msg = "";

    int errorCode = -1;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
