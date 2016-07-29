package com.welink.biz.common.constants;

/**
 * Created by daniel on 14-9-10.
 */
public enum ProfileEnum {

    //成员包含 家属+租客
    proprietor("业主", (byte) 1), member("家属", (byte) 2), renter("租客", (byte) 3), visitor("游客", (byte) 4), rejected("被此房主拒绝", (byte) 5), invalid("无效", (byte) 99);

    // 成员变量
    private String name;

    private byte code;

    // 构造方法
    private ProfileEnum(String name, byte code) {
        this.name = name;
        this.code = code;
    }

    // 普通方法
    public static String getName(int code) {
        for (ProfileEnum c : ProfileEnum.values()) {
            if (Integer.compare(c.getCode(), code) == 0) {
                return c.name;
            }
        }
        return null;
    }

    public static ProfileEnum getEnumByCode(byte code) {
        for (ProfileEnum e : ProfileEnum.values()) {
            if (Byte.compare(e.getCode(), code) == 0) {
                return e;
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