/**
 * Project Name:welink-biz
 * File Name:LevelVO.java
 * Package Name:com.welink.biz
 * Date:2015年11月5日下午5:47:39
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.vo;

import java.math.BigDecimal;

/**
 * ClassName:LevelVO <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月5日 下午5:47:39 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class LevelVO {
	private Integer id;
	private String title;
	private BigDecimal value;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	
}

