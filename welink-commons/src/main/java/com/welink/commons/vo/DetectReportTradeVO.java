package com.welink.commons.vo;

import java.util.Date;

public class DetectReportTradeVO {
	
	private Long id;
	private Long money;
	private Long userId;
	private String userName;
	private String userPicUrl;
	private String userMobile;
	private Long serviceId;
	private String csadName;
	private String csadPicUrl;
	private Long tradeId;
	private Byte status;
	private Date reportDateCreated;
	private Date tradeDateCreated;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getMoney() {
		return money;
	}
	public void setMoney(Long money) {
		this.money = money;
	}
	public String getCsadName() {
		return csadName;
	}
	public void setCsadName(String csadName) {
		this.csadName = csadName;
	}
	public String getCsadPicUrl() {
		return csadPicUrl;
	}
	public void setCsadPicUrl(String csadPicUrl) {
		this.csadPicUrl = csadPicUrl;
	}
	public Long getTradeId() {
		return tradeId;
	}
	public void setTradeId(Long tradeId) {
		this.tradeId = tradeId;
	}
	public Byte getStatus() {
		return status;
	}
	public void setStatus(Byte status) {
		this.status = status;
	}
	public Long getServiceId() {
		return serviceId;
	}
	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}
	public Date getReportDateCreated() {
		return reportDateCreated;
	}
	public void setReportDateCreated(Date reportDateCreated) {
		this.reportDateCreated = reportDateCreated;
	}
	public Date getTradeDateCreated() {
		return tradeDateCreated;
	}
	public void setTradeDateCreated(Date tradeDateCreated) {
		this.tradeDateCreated = tradeDateCreated;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserMobile() {
		return userMobile;
	}
	public void setUserMobile(String userMobile) {
		this.userMobile = userMobile;
	}
	public String getUserPicUrl() {
		return userPicUrl;
	}
	public void setUserPicUrl(String userPicUrl) {
		this.userPicUrl = userPicUrl;
	}
	
}
