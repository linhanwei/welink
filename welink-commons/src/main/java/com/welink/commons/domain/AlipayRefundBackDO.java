package com.welink.commons.domain;

import java.util.Date;

public class AlipayRefundBackDO {
//	notify_time	通知时间	Date	通知发送的时间。格式为：yyyy-MM-dd HH:mm:ss。	不可空	2009-08-12 11:08:32
//	notify_type	通知类型	String	通知的类型。	不可空	batch_refund_notify
//	notify_id	通知校验ID	String	通知校验ID。	不可空	70fec0c2730b27528665af4517c27b95
//	sign_type	签名方式	String	DSA、RSA、MD5三个值可选，必须大写。	不可空	MD5
//	sign	签名	String	请参见签名验证。	不可空	b7baf9af3c91b37bef4261849aa76281
//	batch_no	退款批次号	String	原请求退款批次号。	不可空	20060702001
//	success_num	退款成功总数	String	退交易成功的笔数。0<= success_num<= 总退款笔数。	不可空	2
//	result_details	退款结果明细	String	退款结果明细：
//	退手续费结果返回格式：交易号^退款金额^处理结果$退费账号^退费账户ID^退费金额^处理结果；
//	不退手续费结果返回格式：交易号^退款金额^处理结果。
//	若退款申请提交成功，处理结果会返回“SUCCESS”。若提交失败，退款的处理结果中会有报错码，参见即时到账批量退款业务错误码。	可空	2010031906272929^80^SUCCESS$jax_chuanhang@alipay.com^2088101003147483^0.01^SUCCESS

	  private String discount;
	  private Date notifyTime;
	  private String notifyType;
	  private String notifyId;
	  private String signType;
	  private String sign;
	  private String batchNo;
	  private String successNum;
	  private String resultDetails;
	public String getDiscount() {
		return discount;
	}
	public void setDiscount(String discount) {
		this.discount = discount;
	}
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
	public String getSuccessNum() {
		return successNum;
	}
	public void setSuccessNum(String successNum) {
		this.successNum = successNum;
	}
	public String getResultDetails() {
		return resultDetails;
	}
	public void setResultDetails(String resultDetails) {
		this.resultDetails = resultDetails;
	}
	  
	  
	  
	  
	  
}