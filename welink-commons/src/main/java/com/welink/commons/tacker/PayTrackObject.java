package com.welink.commons.tacker;

import java.util.Date;

/**
 * Created by saarixx on 12/1/15.
 */
public class PayTrackObject {

    // 创建时间
    private Date gmtCreate;
    // 付款时间
    private Date gmtPayment;
    // 推送时间
    private Date notifyTime;
    // 交易价格
    private Integer totalFee;
    // 账户
    private String buyerAccount;
    // 订单号码
    private Long outTradeNo;
    // 微信支付还是支付宝支付，还是线下支付之类的
    private String type;

    private String sellerAccount;

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtPayment() {
        return gmtPayment;
    }

    public void setGmtPayment(Date gmtPayment) {
        this.gmtPayment = gmtPayment;
    }

    public Date getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(Date notifyTime) {
        this.notifyTime = notifyTime;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public String getBuyerAccount() {
        return buyerAccount;
    }

    public void setBuyerAccount(String buyerAccount) {
        this.buyerAccount = buyerAccount;
    }

    public Long getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(Long outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSellerAccount() {
        return sellerAccount;
    }

    public void setSellerAccount(String sellerAccount) {
        this.sellerAccount = sellerAccount;
    }
}
