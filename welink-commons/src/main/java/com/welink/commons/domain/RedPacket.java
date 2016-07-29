package com.welink.commons.domain;

/**
 * Created by daniel on 15-3-10.
 */
public class RedPacket {
    private String beginTime;
    private String endTime;
    private Long num;
    private String timeInfo;
    private String info;
    private String flag;
    
    
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
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
	
}
