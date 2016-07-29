/**
 * Project Name:welink-commons
 * File Name:RewardItem.java
 * Package Name:com.welink.commons.vo
 * Date:2015年12月23日下午4:51:48
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.vo;

import java.util.Date;

/**
 * ClassName:RewardItem <br/>
 * Function: 抽奖奖品VO <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月23日 下午4:51:48 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class RewardItemVO {
	private Long id;
    private Long userId;
    private Integer type;
    private Integer value;
    private String targetId;
    private String destination;
    private Date dateCreated;
    private String itemTitle;
    private Long itemPrice;
    private String pics;//主图图片
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	public String getTargetId() {
		return targetId;
	}
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getItemTitle() {
		return itemTitle;
	}
	public void setItemTitle(String itemTitle) {
		this.itemTitle = itemTitle;
	}
	public Long getItemPrice() {
		return itemPrice;
	}
	public void setItemPrice(Long itemPrice) {
		this.itemPrice = itemPrice;
	}
	public String getPics() {
		return pics;
	}
	public void setPics(String pics) {
		this.pics = pics;
	}
    
    
}

