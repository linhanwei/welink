package com.welink.commons.vo;

import java.util.Date;

/**
 * 
 * ClassName: MikuReturnGoodsVO <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(退货). <br/>
 * date: 2016年1月21日 下午5:35:47 <br/>
 *
 * @author LuoGuangChun
 */
public class MikuReturnGoodsVO {
	
	private Long id;
	
    private Long version;

    private Long tradeId;

    private Long orderId;

    private Long artificialId;

    private Long buyerId;

    private Integer num;

    private String picUrl;

    private Long price;

    private Long totalFee;

    private Long refundFee;

    private Byte status;

    private Date timeoutActionTime;

    private String title;

    private Date dateCreated;

    private Date lastUpdated;

    private Date consignTime;

    private Date finishTime;

    private Long consigneeId;

    private String buyerMemo;

    private String sellerMemo;

    private String returnReason;

    private Date reqExamine;

    private Date receiptTime;

    private Byte isSubsidy;

    private Long subsidyFee;

    private Byte tradeStatus;
    
    /*--------------------新加----------------------------------------*/
    
    private String expressCompany;

    private String expressNo;
    
    private Integer isTimeOut;
    
    private Date orderDateCreated;
    
    String returnName;	//退货人
    String returnMobile;//退货人电话
    String returnUserAddr;//退货人地址

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getTradeId() {
		return tradeId;
	}

	public void setTradeId(Long tradeId) {
		this.tradeId = tradeId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getArtificialId() {
		return artificialId;
	}

	public void setArtificialId(Long artificialId) {
		this.artificialId = artificialId;
	}

	public Long getBuyerId() {
		return buyerId;
	}

	public void setBuyerId(Long buyerId) {
		this.buyerId = buyerId;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
		this.price = price;
	}

	public Long getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(Long totalFee) {
		this.totalFee = totalFee;
	}

	public Long getRefundFee() {
		return refundFee;
	}

	public void setRefundFee(Long refundFee) {
		this.refundFee = refundFee;
	}

	public Byte getStatus() {
		return status;
	}

	public void setStatus(Byte status) {
		this.status = status;
	}

	public Date getTimeoutActionTime() {
		return timeoutActionTime;
	}

	public void setTimeoutActionTime(Date timeoutActionTime) {
		this.timeoutActionTime = timeoutActionTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Date getConsignTime() {
		return consignTime;
	}

	public void setConsignTime(Date consignTime) {
		this.consignTime = consignTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public Long getConsigneeId() {
		return consigneeId;
	}

	public void setConsigneeId(Long consigneeId) {
		this.consigneeId = consigneeId;
	}

	public String getBuyerMemo() {
		return buyerMemo;
	}

	public void setBuyerMemo(String buyerMemo) {
		this.buyerMemo = buyerMemo;
	}

	public String getSellerMemo() {
		return sellerMemo;
	}

	public void setSellerMemo(String sellerMemo) {
		this.sellerMemo = sellerMemo;
	}

	public String getReturnReason() {
		return returnReason;
	}

	public void setReturnReason(String returnReason) {
		this.returnReason = returnReason;
	}

	public Date getReqExamine() {
		return reqExamine;
	}

	public void setReqExamine(Date reqExamine) {
		this.reqExamine = reqExamine;
	}

	public Date getReceiptTime() {
		return receiptTime;
	}

	public void setReceiptTime(Date receiptTime) {
		this.receiptTime = receiptTime;
	}

	public Byte getIsSubsidy() {
		return isSubsidy;
	}

	public void setIsSubsidy(Byte isSubsidy) {
		this.isSubsidy = isSubsidy;
	}

	public Long getSubsidyFee() {
		return subsidyFee;
	}

	public void setSubsidyFee(Long subsidyFee) {
		this.subsidyFee = subsidyFee;
	}

	public Byte getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(Byte tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public String getExpressCompany() {
		return expressCompany;
	}

	public void setExpressCompany(String expressCompany) {
		this.expressCompany = expressCompany;
	}

	public String getExpressNo() {
		return expressNo;
	}

	public void setExpressNo(String expressNo) {
		this.expressNo = expressNo;
	}

	public Integer getIsTimeOut() {
		return isTimeOut;
	}

	public void setIsTimeOut(Integer isTimeOut) {
		this.isTimeOut = isTimeOut;
	}

	public Date getOrderDateCreated() {
		return orderDateCreated;
	}

	public void setOrderDateCreated(Date orderDateCreated) {
		this.orderDateCreated = orderDateCreated;
	}

	public String getReturnName() {
		return returnName;
	}

	public void setReturnName(String returnName) {
		this.returnName = returnName;
	}

	public String getReturnMobile() {
		return returnMobile;
	}

	public void setReturnMobile(String returnMobile) {
		this.returnMobile = returnMobile;
	}

	public String getReturnUserAddr() {
		return returnUserAddr;
	}

	public void setReturnUserAddr(String returnUserAddr) {
		this.returnUserAddr = returnUserAddr;
	}

}