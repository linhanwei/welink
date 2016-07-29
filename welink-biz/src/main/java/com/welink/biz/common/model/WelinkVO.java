package com.welink.biz.common.model;

import java.util.HashMap;
import java.util.Map;

/**
 * json vo
 * Created by daniel on 15-1-5.
 */
public class WelinkVO {

    int status = 0;

    Map result = new HashMap();

    String msg;

    int code;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map getResult() {
        return result;
    }

    public void setResult(Map result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
