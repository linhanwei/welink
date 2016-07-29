package com.welink.biz.common.model;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by daniel on 15-4-27.
 */
public class TradeViewDO {
    Byte status;
    Byte payType;
    String tradeId;
    String tradeMsg;
    Long price;
    Long point;
    Long coupon;
    Long totalFee;
    Byte tradeType;
    Long postFee;
    Long payment;
    Long dateCreated;//下单时间
    Long appointDTime;//预约配送时间
    Long payTime;//付款时间
    Long consignTime;//备货时间
    Long confirmTime;//配送时间
    Long endTime;//结束时间
    String deliverMobile;//配送员电话
    String deliverName;//配送员姓名
    Byte shippingType;//快递方式 是否自提
    Long communityId;
    String communityName;//站点名称
    String communityMobile;//站点电话
    Byte canRate;	//是否可评价(0=不能评价;1=可评价;)
    Byte buyerRate;	//是否评价(0=未评价;1=已评价;)

    List<OrderViewDO> orderViewDOs = Lists.newArrayList();

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getCommunityMobile() {
        return communityMobile;
    }

    public void setCommunityMobile(String communityMobile) {
        this.communityMobile = communityMobile;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Byte getShippingType() {
        return shippingType;
    }

    public void setShippingType(Byte shippingType) {
        this.shippingType = shippingType;
    }

    public Long getPayTime() {
        return payTime;
    }

    public void setPayTime(Long payTime) {
        this.payTime = payTime;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public String getDeliverName() {
        return deliverName;
    }

    public void setDeliverName(String deliverName) {
        this.deliverName = deliverName;
    }

    public String getDeliverMobile() {
        return deliverMobile;
    }

    public void setDeliverMobile(String deliverMobile) {
        this.deliverMobile = deliverMobile;
    }

    public Long getConsignTime() {
        return consignTime;
    }

    public void setConsignTime(Long consignTime) {
        this.consignTime = consignTime;
    }

    public Long getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(Long confirmTime) {
        this.confirmTime = confirmTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public List<OrderViewDO> getOrderViewDOs() {
        return orderViewDOs;
    }

    public void setOrderViewDOs(List<OrderViewDO> orderViewDOs) {
        this.orderViewDOs = orderViewDOs;
    }

    public Long getAppointDTime() {
        return appointDTime;
    }

    public void setAppointDTime(Long appointDTime) {
        this.appointDTime = appointDTime;
    }

    public Long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Byte getPayType() {
        return payType;
    }

    public void setPayType(Byte payType) {
        this.payType = payType;
    }

    public String getTradeMsg() {
        return tradeMsg;
    }

    public void setTradeMsg(String tradeMsg) {
        this.tradeMsg = tradeMsg;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getPoint() {
        return point;
    }

    public void setPoint(Long point) {
        this.point = point;
    }

    public Long getCoupon() {
        return coupon;
    }

    public void setCoupon(Long coupon) {
        this.coupon = coupon;
    }

    public Long getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Long totalFee) {
        this.totalFee = totalFee;
    }

    public Byte getTradeType() {
        return tradeType;
    }

    public void setTradeType(Byte tradeType) {
        this.tradeType = tradeType;
    }

    public Long getPostFee() {
        return postFee;
    }

    public void setPostFee(Long postFee) {
        this.postFee = postFee;
    }

    public Long getPayment() {
        return payment;
    }

    public void setPayment(Long payment) {
        this.payment = payment;
    }

	public Byte getCanRate() {
		return canRate;
	}

	public void setCanRate(Byte canRate) {
		this.canRate = canRate;
	}

	public Byte getBuyerRate() {
		return buyerRate;
	}

	public void setBuyerRate(Byte buyerRate) {
		this.buyerRate = buyerRate;
	}
}
