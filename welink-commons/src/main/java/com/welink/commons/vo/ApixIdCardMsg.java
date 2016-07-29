/**
 * Project Name:welink-commons
 * File Name:ApixIdCardMsg.java
 * Package Name:com.welink.commons.vo
 * Date:2016年3月2日下午2:23:34
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.vo;


/**
 * ClassName:ApixIdCardMsg <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年3月2日 下午2:23:34 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class ApixIdCardMsg {
	private String msg;
	private Integer code;
	private ApixIdCardMsgData data;
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public ApixIdCardMsgData getData() {
		return data;
	}
	public void setData(ApixIdCardMsgData data) {
		this.data = data;
	}
}

