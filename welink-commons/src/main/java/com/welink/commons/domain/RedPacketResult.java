package com.welink.commons.domain;

/**
 * Created by daniel on 15-3-10.
 */
public class RedPacketResult {
    private String beginTime;
    private String endTime;
    private Long num;
    private String timeInfo;
    private String info;
    private String flag;
    private String result;
    private String randnum;
    private String percent;
    private String price;
    private String openid;
    
    
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	public Long getNum() {
		return num;
	}
	public void setNum(Long num) {
		this.num = num;
	}
	public String getTimeInfo() {
		return timeInfo;
	}
	public void setTimeInfo(String timeInfo) {
		this.timeInfo = timeInfo;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getRandnum() {
		return randnum;
	}
	public void setRandnum(String randnum) {
		this.randnum = randnum;
	}
	public String getPercent() {
		return percent;
	}
	public void setPercent(String percent) {
		this.percent = percent;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
}
