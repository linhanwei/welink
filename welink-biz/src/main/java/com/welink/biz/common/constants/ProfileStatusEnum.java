package com.welink.biz.common.constants;

/**
 * Created by daniel on 14-9-10.
 */
public enum ProfileStatusEnum {
    valid("有效", (byte) 1), removed("被移除", (byte) 0), deleted("被删除", (byte) 2);
    // 成员变量
    private String name;
    private byte code;

    // 构造方法
    private ProfileStatusEnum(String name, byte code) {
        this.name = name;
        this.code = code;
    }

    // 普通方法
    public static String getName(int code) {
        for (ProfileEnum c : ProfileEnum.values()) {
            if (Integer.compare(c.getCode(), code) == 0) {
                return c.name();
            }
        }
        return null;
    }

    // get set 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }
}
