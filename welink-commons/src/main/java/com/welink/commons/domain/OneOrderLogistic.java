package com.welink.commons.domain;

import java.util.List;


//整个的物流信息的java bean
public class OneOrderLogistic {
	private String companyName;
	private String companyNum;
	private String state;
	private List<Item> itemist;
	private List<data> wllist;
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getCompanyNum() {
		return companyNum;
	}
	public void setCompanyNum(String companyNum) {
		this.companyNum = companyNum;
	}
	public List<Item> getItemist() {
		return itemist;
	}
	public void setItemist(List<Item> itemist) {
		this.itemist = itemist;
	}
	public List<data> getWllist() {
		return wllist;
	}
	public void setWllist(List<data> wllist) {
		this.wllist = wllist;
	}
	
}
