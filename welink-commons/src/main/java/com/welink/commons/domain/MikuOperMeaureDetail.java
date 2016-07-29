package com.welink.commons.domain;

import java.math.BigDecimal;
import java.util.List;


//具体时间的统计
public class MikuOperMeaureDetail {
	private String timetype;
	private BigDecimal sumCount;
	//测试部位
	private Byte type;
	//4个基本常量
	private String numstype;
	
	private List<MikuOperMeaureData> list;
	public String getTimetype() {
		return timetype;
	}
	public String getNumstype() {
		return numstype;
	}
	public void setNumstype(String numstype) {
		this.numstype = numstype;
	}
	public void setTimetype(String timetype) {
		this.timetype = timetype;
	}
	public BigDecimal getSumCount() {
		return sumCount;
	}
	public void setSumCount(BigDecimal sumCount) {
		this.sumCount = sumCount;
	}
	public List<MikuOperMeaureData> getList() {
		return list;
	}
	public void setList(List<MikuOperMeaureData> list) {
		this.list = list;
	}
	public Byte getType() {
		return type;
	}
	public void setType(Byte type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "MikuOperMeaureDetail [timetype=" + timetype + ", sumCount="
				+ sumCount + ", type=" + type + ", numstype=" + numstype
				+ ", list=" + list + "]";
	}
	
	
}
