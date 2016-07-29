package com.welink.commons.domain;

import java.util.Date;

public class AlipayBatchTransBackDO {
//	notify_time	通知时间	Date	通知发送的时间。格式为：yyyy-MM-dd HH:mm:ss。	不可空	2009-08-12 11:08:32
//	notify_type	通知类型	String	通知的类型。	不可空	batch_refund_notify
//	notify_id	通知校验ID	String	通知校验ID。	不可空	70fec0c2730b27528665af4517c27b95
//	sign_type	签名方式	String	DSA、RSA、MD5三个值可选，必须大写。	不可空	MD5
//	sign	签名	String	请参见签名验证。	不可空	b7baf9af3c91b37bef4261849aa76281
//	batch_no	退款批次号	String	原请求退款批次号。	不可空	20060702001
//	pay_user_id	付款账号ID	String	付款的支付宝账号对应的支付宝唯一用户号。 以2088开头的16位纯数字组成。	不可空	2088002464631181
//	pay_user_name	付款账号姓名	String	付款账号姓名。	不可空	毛毛
//	pay_account_no	付款账号	String	付款账号。	不可空	20880024646311810156
//	success_details	转账成功的详细信息	String	批量付款中成功付款的信息。 格式为：流水号^收款方账号^收款账号姓名^付款金额^成功标识(S)^成功原因(null)^支付宝内部流水号^完成时间。 每条记录以“|”间隔。	可空	0315001^gonglei1@handsome.com.cn^龚本林^20.00^S^null^200810248427067^20081024143652|
//	fail_details	转账失败的详细信息	String	批量付款中未成功付款的信息。 格式为：流水号^收款方账号^收款账号姓名^付款金额^失败标识(F)^失败原因^支付宝内部流水号^完成时间。 每条记录以“|”间隔。	可空	0315006^xinjie_xj@163.com^星辰公司1^20.00^F^TXN_RESULT_TRANSFER_OUT_CAN_NOT_EQUAL_IN^200810248427065^20081024143651
	  private Date  notifyTime;
	  private String notifyType;
	  private String notifyId;
	  private String signType;
	  private String sign;
	  private String batchNo;
	  private String payUserId;
	  private String payUserName;
	  private String payAccountNo;
	  private String successDetails;
	  private String failDetails;
	  
	public Date getNotifyTime() {
		return notifyTime;
	}
	public void setNotifyTime(Date notifyTime) {
		this.notifyTime = notifyTime;
	}
	public String getNotifyType() {
		return notifyType;
	}
	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}
	public String getNotifyId() {
		return notifyId;
	}
	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}
	public String getSignType() {
		return signType;
	}
	public void setSignType(String signType) {
		this.signType = signType;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public String getPayUserId() {
		return payUserId;
	}
	public void setPayUserId(String payUserId) {
		this.payUserId = payUserId;
	}
	public String getPayUserName() {
		return payUserName;
	}
	public void setPayUserName(String payUserName) {
		this.payUserName = payUserName;
	}
	public String getPayAccountNo() {
		return payAccountNo;
	}
	public void setPayAccountNo(String payAccountNo) {
		this.payAccountNo = payAccountNo;
	}
	public String getSuccessDetails() {
		return successDetails;
	}
	public void setSuccessDetails(String successDetails) {
		this.successDetails = successDetails;
	}
	public String getFailDetails() {
		return failDetails;
	}
	public void setFailDetails(String failDetails) {
		this.failDetails = failDetails;
	}
	  
	
	  
	  
	  
	  
}