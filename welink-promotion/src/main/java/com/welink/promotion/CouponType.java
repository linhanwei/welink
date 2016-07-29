package com.welink.promotion;

/**
 * Created by saarixx on 10/3/15.
 */
public enum CouponType {


    OFF_FOR_CONDITION(2001, "满减优惠券"),

    ROOKIE(2002, "新人优惠"),
    
    INVITE(2003, "邀请"),


    // ------------------------------------
    ;
    private int code;

    private String description;

    CouponType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
