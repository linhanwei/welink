package com.welink.promotion;

/**
 * Created by XUTIAN on 2015/3/9.
 */
public enum PromotionErrorEnum {

    //积分相关错误码
    POINT_ALREADY_RECEIVE("积分曾可贵，时间价更高，明儿再来~", 1001),

    POINT_USE_ERROR_TARGET("请联系我们，使用积分的时候没有关联订单", 1002),
    POINT_USE_ERROR_MORE_THAN_AVAILABLE("请联系我们，使用的积分大于可用积分", 1003),
    POINT_USE_ERROR_MORE_THAN_FROZEN("请联系我们，清除冻结的积分大于已冻结积分", 1004),

    POINT_ROOKIE_PRESENT_ALREADY_RECEIVED("新人积分已经领取...", 1005),


    //优惠券相关错误码
    COUPON_LACK_LUCKY("人品槽未满～继续加油！", 2001),
    COUPON_SHAKE_LIMITED("优惠券虽好，可不要贪心哟", 2002),
    COUPON_BINGO_LIMITED("再摇我就晕了～", 2003),
    COUPON_HACKED("把手机放下，有什么冲我来！！", 2004),
    COUPON_USE_ERROR_TYPE("请联系我们，使用优惠券只能直接消费或冻结", 2005),
    COUPON_UNFROZEN_ERROR_TYPE("请联系我们，已冻结的优惠券只能变成已使用", 2006),
    COUPON_USE_BEFORE_START_TIME("时辰过早，大人稍等", 2007),
    COUPON_USE_AFTER_END_TIME("呜～一不小心过期了..", 2008),
    COUPON_UNDER_MIN_VALUE("优惠卷不是你想用，想用就能用...", 2009),

    COUPON_ROOKIE_PRESENT_ALREADY_RECEIVED("新人优惠券已经领取...", 2010),
    
    LOTTERY_DRAW_LIMITED("再抽我就晕了～", 3001),


    // ------------------------------------------------
    ;
    // 成员变量
    private String msg;

    private int code;

    // 构造方法
    private PromotionErrorEnum(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    // 普通方法
    public static String getMsg(int code) {
        for (PromotionErrorEnum c : PromotionErrorEnum.values()) {
            if (Integer.compare(c.getCode(), code) == 0) {
                return c.msg;
            }
        }
        return "";
    }

    // get set 方法
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

}
