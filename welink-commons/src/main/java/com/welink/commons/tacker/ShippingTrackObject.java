package com.welink.commons.tacker;

import java.util.Date;

/**
 * 调度人员调度，配送人员开始配送，配送人员确认收货或者用户确认收货的统计信息
 * <p/>
 * Created by saarixx on 20/1/15.
 */
public class ShippingTrackObject {
    /**
     * 商品Id
     */
    private Long ItemId;
    /**
     * 交易Id
     */
    private Long tradeId;
    /**
     * 交易是否有特殊处理
     */
    private Boolean isSpecial;
    /**
     * 哪个配送点
     */
    private Long communityId;
    /**
     * 调度人员Id
     */
    private Long dispatcherId;
    /**
     * 配送人员Id
     */
    private Long courierId;
    /**
     * 订单创建时间
     */
    private Date tradeCreated;
    /**
     * 订单调度时间
     */
    private Date tradeDispatched;
    /**
     * 订单调度和创建时间差
     */
    private Long timeDispatchedMinusCreated;
    /**
     * 配送人员扫码时间
     */
    private Date tradeShipped;
    /**
     * 订单开始派送和调度时间差
     */
    private Long timeShippedMinusDispatched;
    /**
     * 用户/配送确认收货时间
     */
    private Date tradeReceived;
    /**
     * 订单接收和配送时间差
     */
    private Long timeReceivedMinusShipped;
    /**
     * 订单接收和创建时间差
     */
    private Long timeReceivedMinusCreated;
    /**
     * 用户确认收货1，配送人员确认收货2
     */
    private Integer tradeReceivedType;
    /**
     * 当前是在什么状态 DISPATCHED; SHIPPED; RECEIVED;
     */
    private String step;


}
