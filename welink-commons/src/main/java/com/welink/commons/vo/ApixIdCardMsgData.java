/**
 * Project Name:welink-commons
 * File Name:ApixIdCardMsgData.java
 * Package Name:com.welink.commons.vo
 * Date:2016年3月2日下午2:24:30
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.vo;

import java.util.Date;

/**
 * ClassName:ApixIdCardMsgData <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年3月2日 下午2:24:30 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class ApixIdCardMsgData {
	private String cardno;
	private String name;
	private String moible_prov;
	private String sex;
	private Date birthday;
	private String address;
	private String mobile_operator;
	private String mobile_city;
	public String getMoible_prov() {
		return moible_prov;
	}
	public void setMoible_prov(String moible_prov) {
		this.moible_prov = moible_prov;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getMobile_operator() {
		return mobile_operator;
	}
	public void setMobile_operator(String mobile_operator) {
		this.mobile_operator = mobile_operator;
	}
	public String getMobile_city() {
		return mobile_city;
	}
	public void setMobile_city(String mobile_city) {
		this.mobile_city = mobile_city;
	}
	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}

