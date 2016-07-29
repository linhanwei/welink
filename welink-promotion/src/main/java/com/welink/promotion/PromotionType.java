package com.welink.promotion;

/**
 * 交互动作，能够产生的优惠动作如下
 * <p/>
 * Created by saarixx on 5/3/15.
 */
public enum PromotionType {

    // 积分领取或者使用

    POINT_SIGN_IN(100001, "签到领积分", 1),

    POINT_NEW_USER(100002, "新用户发积分", 1),

    POINT_SHARE(100003, "分享领积分", 1),
    
    POINT_TRADE_SUCCESS(100004, "交易成功领取积分", 1),
    
    POINT_INVITE(100005, "邀请领积分", 1),
    
    POINT_LOTTERY_DRAW(100006, "抽奖领积分", 1),

    POINT_USE_IN_TRADE_DIRECT(101001, "交易直接使用积分", -1),

    POINT_USE_IN_TRADE_FROZEN(101002, "交易冻结积分", -1),

    POINT_USE_IN_TRADE_FROZEN_ELIMINATE(101003, "交易解冻积分清零", -1),

    POINT_USE_IN_TRADE_FROZEN_RESTORE(101004, "交易解冻积分还原", 1),


    // 满减优惠券的领取或者使用

    COUPON_SIGN_IN(200001, "签到领优惠券", 1),

    COUPON_NEW_USER(200002, "新用户发优惠券", 1),

    COUPON_SHAKE(200003, "摇一摇领取优惠券", 1),
    
    COUPON_INVITE(200005, "邀请领取优惠券", 1),
    
    COUPON_LOTTERY_DRAW(200006, "抽奖领取优惠券", 1),
    
    COUPON_USE_IN_TRADE_DIRECT(201001, "交易使用优惠券", -1),

    COUPON_USE_IN_TRADE_FROZEN(201002, "交易冻结优惠券", -1),

    COUPON_USE_IN_TRADE_FROZEN_ELIMINATE(201003, "交易解冻优惠券，变成已使用", -1),

    COUPON_USE_IN_TRADE_FROZEN_RESTORE(201004, "交易解冻优惠券，变成未使用", 1),
    
    // 赠品对商品的操作
    ITEM_ENOUGY_NUM_STATUS(300001,"增品操作",-1),
    CHANCE_LOTTERY_DRAW(300002, "抽奖机会", 1),
    USE_CHANCE_LOTTERY_DRAW(300003, "使用抽奖机会", 1),
    ITEM_LOTTERY_DRAW(300006,"抽奖领取商品", 1),		
    
    //活动发放优惠券
    COUPON_SEND_ACTIVE_900001(900001, "发放国足活动优惠券", 1),

    // ------------------------------------
    ;
    private int code; 

    private String description;

    private int symbol;

    PromotionType(int code, String description, int symbol) {
        this.code = code;
        this.description = description;
        this.symbol = symbol;
    }

    // 普通方法
    public static String getMsg(int code) {
        for (PromotionType c : PromotionType.values()) {
            if (Integer.compare(c.getCode(), code) == 0) {
                return c.description;
            }
        }
        return "";
    }

    // 普通方法
    public static int getSymbol(int code) {
        for (PromotionType c : PromotionType.values()) {
            if (Integer.compare(c.getCode(), code) == 0) {
                return c.symbol;
            }
        }
        return -1;
    }

    public int getSymbol() {
        return symbol;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
