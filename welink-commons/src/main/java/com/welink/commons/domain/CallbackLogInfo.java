package com.welink.commons.domain;

public class CallbackLogInfo {
	
	
	private String status;
	private String billstatus;
	private String message;
	private SearchLogisticBean lastResult;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getBillstatus() {
		return billstatus;
	}
	public void setBillstatus(String billstatus) {
		this.billstatus = billstatus;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public SearchLogisticBean getLastResult() {
		return lastResult;
	}
	public void setLastResult(SearchLogisticBean lastResult) {
		this.lastResult = lastResult;
	}
	@Override
	public String toString() {
		return "CallbackLogInfo [status=" + status + ", billstatus="
				+ billstatus + ", message=" + message + ", lastResult="
				+ lastResult + "]";
	}
}
