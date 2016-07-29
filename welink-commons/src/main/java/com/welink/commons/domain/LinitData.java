package com.welink.commons.domain;

public class LinitData {
	private String com;
	private String num;
	private String from;
	private String to;
	
	
	public LinitData(String com, String num) {
		super();
		this.com = com;
		this.num = num;
	}
	public String getCom() {
		return com;
	}
	public void setCom(String com) {
		this.com = com;
	}
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	@Override
	public String toString() {
		return "LinitData [com=" + com + ", num=" + num + ", from=" + from
				+ ", to=" + to + "]";
	}
	
}
