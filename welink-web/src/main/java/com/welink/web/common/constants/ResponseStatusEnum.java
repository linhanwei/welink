package com.welink.web.common.constants;

/**
 * Created by daniel on 14-9-16.
 */
public enum ResponseStatusEnum {

    SUCCESS((byte) 1, "成功"),

    FAILED((byte) 0, "系统繁忙，请稍后再试");

    // 成员变量
    private byte code;

    private String msg;

    // 构造方法
    private ResponseStatusEnum(byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    // get set 方法
    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
