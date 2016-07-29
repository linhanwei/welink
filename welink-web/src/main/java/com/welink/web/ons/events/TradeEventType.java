package com.welink.web.ons.events;

/**
 * Created by spider on 15/7/13.
 */
public enum TradeEventType {

    TRADE_CREATE("TradeCreate"), // 创建订单
    TRADE_BUYER_PAY("TradeBuyerPay"), // 已付款
    TRADE_PACKAGE("TradePackage"), // 已配货
    TRADE_SELLER_SHIP("TradeSellerShip"), // 已发货
    TRADE_SUCCESS("TradeSuccess"), // 已签收
    TRADE_CLOSE("TradeClose"), // 未付款订单关闭
    TRADE_CLOSE_WITH_REFUND("TradeCloseWithRefund"), // 订单关闭，有退款

    // -------------------
    ;

    private String topic;

    TradeEventType(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public static TradeEventType findByStatus(String topic) {
        for (TradeEventType tradeEventType : values()) {
            if (tradeEventType.topic.equalsIgnoreCase(topic)) {
                return tradeEventType;
            }
        }
        return null;
    }

}
