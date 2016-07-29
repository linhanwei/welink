package com.welink.buy.utils;

/**
 * 下单时候的错误提示
 * <p/>
 * Created by saarixx on 17/9/14.
 */
public enum BuyItemResultCode {

    BUY_ITEM_ERROR_UNKNOWN(-1, "非常抱歉，系统忙，请稍后再试"),

    BUY_ITEM_ERROR_ITEM_NOT_EXSIST(-4, "非常抱歉，该宝贝不存在"),

    BUY_ITEM_ERROR_ITEM_LESS(-13, "非常抱歉，您要购买的商品库存不足"),

    BUY_ITEM_ERROR_INVALID_NUMBER(-104, "非常抱歉，您输入的出价金额无效,请不要包含数字以外的字符"),

    //BUY_ITEM_ERROR_QUANTITY_TOO_LARGE(-107, "非常抱歉，您输入的数量超过了可购买的最大数量"),
    BUY_ITEM_ERROR_QUANTITY_TOO_LARGE(-107, "亲，您最多可购买"),	//限购

    BUY_ITEM_ERROR_QUANTITY_TOO_SMALL(-108, "好难过啊，您为什么一件都不买呢？"),

    RESULT_ITEM_NOT_ALLOW_QUANTITY(-109, "亲，库存不足了啦T_T，下次再来吧~"),
    
    BUY_ITEM_ERROR_PANIC_NOT_START(-110, "亲，这么急干嘛呢？还没开始呀~"),	//
    
    BUY_ITEM_ERROR_PANIC_IS_END(-111, "活动已经结束啦，下次趁早哟~"),	//
    
    BUY_ITEM_ERROR_PANIC_ITEM_LESS(-112, "都被抢完啦T_T，下次早点来吧，亲~"),	//

    BUY_ITEM_ERROR_NOT_START(-203, "非常抱歉，您要购买的商品还未上架"),

    BUY_ITEM_ERROR(-200, "非常抱歉，您要购买的商品未上架或已删除或超出可购买数量"),

    BUY_ITEM_INVALIDATE(-905, "非常抱歉，您要购买的商品已下架或删除"),

    PARAM_IS_ERROR(-906, "非常抱歉，您的输入有误，无法购买"),

    UN_SUPPORT_COD(-10001, "非常抱歉，此宝贝不支持货到付款"),

    LIMIT_PROMOTION_TIMEOUT(10007, "非常抱歉，限时拍宝贝已经过期"),

    TOTAL_FEE_TOO_LARGE(-703, "非常抱歉，您购买的宝贝总价过高"),

    SECKILL_ACCESS_MAX_PER_MACHINE_NUM(1160, "非常抱歉，购买人数过多，请再试一次"),

    CANNOT_CLOSE_TRADE(-2, "非常抱歉，不能关闭已经安排配送或付款成功的订单"),

    MAINTAIN_TIME(7777, "非常抱歉，系统维护中，暂不能下单"),

    CLOSE_MUST_PMF_BILL_FAILED(100020, "非常抱歉，关闭订单失败(更新必缴物业费订单失败)"),

    CLOSE_PRE_PMF_BILL_FAILED(100021, "非常抱歉，创建订单失败(更新预缴物业费订单失败)"),

    UPDATE_PRE_PMF_MUST_BILL_FAILED(100022, "非常抱歉，创建订单失败(更新必缴物业费订单失败)"),

    UPDATE_PMF_BILL_FAILED(100023, "非常抱歉，更新物业订单失败"),

    OFFLINE_PAY_CLOSE_TRADE_FAILED(100024, "非常抱歉，更新订单失败(更新预缴物业费订单失败)"),

    OFFLINE_PAY_CLOSE_ORDER_FAILED(100025, "非常抱歉，更新订单失败"),

    OFFLINE_PAY_UPDATE_BUILDING_FAILED(100026, "非常抱歉，更新房屋信息失败"),

    OFFLINE_PAY_UPDATE_PRE_BILL_FAILED(100027, "非常抱歉，更新预缴订单失败"),

    CREATE_TRADE_UPDATE_ITEM_STOCK_FAILED(100028, "非常抱歉，订单创建失败，请稍后再试"),

    UPDATE_TRADE_TO_TRADE_CLOSE_FAILED(100029, "非常抱歉，更新订单状态失败，请稍后再试"),

    ORDER_SNAPSHOT_FAILED(100030, "非常抱歉，保存订单信息时报，请稍后再试"),

    INPUT_POINT_TOO_LARGE(100031, "非常抱歉，您输入的积分数大于您拥有的积分"),

    COUPON_PARAM_ERROR(100032, "非常抱歉，优惠券参数出错"),

    PAY_TOO_LARGE(100033, "啊哦~，您付的钱大于订单总金额"),

    CREATE_TRADE_FAILED(100034, "啊哦~，创建订单失败，请联系我们"),

    NOT_IN_SERVICE_ZONE(100035, "啊哦~，您选择的收货地址尚未开通配送服务~"),

    POINT_TOO_MORE_SERVICE_ZONE(100036, "啊哦~，您使用了过多的积分数~"),
    
    NOT_LOTTERY_DRAW_REWARD(100037, "啊哦~，您没有抽中奖或奖品已超过七天未领取~"),
    
    NOT_CROWDFUND_ITEM(100038, "啊哦~此商品不是当前众筹商品"),
    ;


    private int code;
    private String message;

    BuyItemResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
