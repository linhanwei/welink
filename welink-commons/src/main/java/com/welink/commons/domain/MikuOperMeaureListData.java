package com.welink.commons.domain;

import java.math.BigDecimal;
import java.util.List;


//这个是物流的节点信息
public class MikuOperMeaureListData {
	String time;
	List<MikuOperMeaureData> list;
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<MikuOperMeaureData> getList() {
		return list;
	}
	public void setList(List<MikuOperMeaureData> list) {
		this.list = list;
	}
	@Override
	public String toString() {
		return "MikuOperMeaureListData [time=" + time + ", list=" + list + "]";
	}
}
