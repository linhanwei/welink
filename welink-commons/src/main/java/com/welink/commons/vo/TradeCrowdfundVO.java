package com.welink.commons.vo;

import java.util.Date;


/**
 * Created by daniel on 15-4-27.
 */
public class TradeCrowdfundVO {
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
    Byte crowdfundRefundStatus;	//众筹退款状态(0=正常;1=退款中;2=已退款)
    
    String receiptName;	//收货人
    String receiptMobile;//收货人电话
    String receiptAddr;//收货人地址
    
    Long itemId;		//商品id
    String itemName;	//商品名称
    String itemPicUrls;	//商品图片
    
    Long crowdfundId;		//众筹id
    String crowdTitle;	//众筹名称
    String crowdPicUrls;		//众筹图片
    Long crowdTargetAmount;	//众筹目标金额
    Long crowdTotalFee;	//众筹已筹金额
    Date crowdEndTime;	//众筹结束时间
    Integer crowdSoldNum;	//众筹支持数
    Integer crowdPlusDay;	//众筹结束后多少天后发货
    Byte crowdStatus;	//众筹状态(-1=无效;0=正常;1=成功;2=失败)
    
    Long crowdDetailId;		//众筹明细id
    Integer crowdDetailSoldNum;	//众筹明细支持数
    String crowdReturnContent;	//众筹明细回报内容
    
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

	public String getCrowdTitle() {
		return crowdTitle;
	}

	public void setCrowdTitle(String crowdTitle) {
		this.crowdTitle = crowdTitle;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Long getCrowdTargetAmount() {
		return crowdTargetAmount;
	}

	public void setCrowdTargetAmount(Long crowdTargetAmount) {
		this.crowdTargetAmount = crowdTargetAmount;
	}

	public Long getCrowdTotalFee() {
		return crowdTotalFee;
	}

	public void setCrowdTotalFee(Long crowdTotalFee) {
		this.crowdTotalFee = crowdTotalFee;
	}

	public Date getCrowdEndTime() {
		return crowdEndTime;
	}

	public void setCrowdEndTime(Date crowdEndTime) {
		this.crowdEndTime = crowdEndTime;
	}

	public Integer getCrowdSoldNum() {
		return crowdSoldNum;
	}

	public void setCrowdSoldNum(Integer crowdSoldNum) {
		this.crowdSoldNum = crowdSoldNum;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Integer getCrowdDetailSoldNum() {
		return crowdDetailSoldNum;
	}

	public void setCrowdDetailSoldNum(Integer crowdDetailSoldNum) {
		this.crowdDetailSoldNum = crowdDetailSoldNum;
	}

	public Long getCrowdfundId() {
		return crowdfundId;
	}

	public void setCrowdfundId(Long crowdfundId) {
		this.crowdfundId = crowdfundId;
	}

	public Long getCrowdDetailId() {
		return crowdDetailId;
	}

	public void setCrowdDetailId(Long crowdDetailId) {
		this.crowdDetailId = crowdDetailId;
	}

	public String getItemPicUrls() {
		return itemPicUrls;
	}

	public void setItemPicUrls(String itemPicUrls) {
		this.itemPicUrls = itemPicUrls;
	}

	public Integer getCrowdPlusDay() {
		return crowdPlusDay;
	}

	public void setCrowdPlusDay(Integer crowdPlusDay) {
		this.crowdPlusDay = crowdPlusDay;
	}

	public String getCrowdReturnContent() {
		return crowdReturnContent;
	}

	public void setCrowdReturnContent(String crowdReturnContent) {
		this.crowdReturnContent = crowdReturnContent;
	}

	public String getReceiptName() {
		return receiptName;
	}

	public void setReceiptName(String receiptName) {
		this.receiptName = receiptName;
	}

	public String getReceiptMobile() {
		return receiptMobile;
	}

	public void setReceiptMobile(String receiptMobile) {
		this.receiptMobile = receiptMobile;
	}

	public String getReceiptAddr() {
		return receiptAddr;
	}

	public Byte getCrowdfundRefundStatus() {
		return crowdfundRefundStatus;
	}

	public void setCrowdfundRefundStatus(Byte crowdfundRefundStatus) {
		this.crowdfundRefundStatus = crowdfundRefundStatus;
	}

	public void setReceiptAddr(String receiptAddr) {
		this.receiptAddr = receiptAddr;
	}

	public String getCrowdPicUrls() {
		return crowdPicUrls;
	}

	public void setCrowdPicUrls(String crowdPicUrls) {
		this.crowdPicUrls = crowdPicUrls;
	}

	public Byte getCrowdStatus() {
		return crowdStatus;
	}

	public void setCrowdStatus(Byte crowdStatus) {
		this.crowdStatus = crowdStatus;
	}

}
