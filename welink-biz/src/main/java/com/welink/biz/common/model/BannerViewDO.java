package com.welink.biz.common.model;

import java.util.Date;

/**
 * Created by daniel on 15-1-23.
 */
public class BannerViewDO {
    String picUrl;
    String target;
    String title;
    /*int type;
    int redirectType;
    int weight;*/
    Integer type;
    Integer redirectType;
    Integer weight;
    
    Integer soldCount;
    String description;
    Byte showText;
    Long categoryId;
    Date onlineEndTime;
    Date onlineStartTime;
    Integer itemNum;

    public Integer getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Byte getShowText() {
		return showText;
	}

	public void setShowText(Byte showText) {
		this.showText = showText;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public Date getOnlineEndTime() {
		return onlineEndTime;
	}

	public void setOnlineEndTime(Date onlineEndTime) {
		this.onlineEndTime = onlineEndTime;
	}

	public Date getOnlineStartTime() {
		return onlineStartTime;
	}

	public void setOnlineStartTime(Date onlineStartTime) {
		this.onlineStartTime = onlineStartTime;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getRedirectType() {
		return redirectType;
	}

	public void setRedirectType(Integer redirectType) {
		this.redirectType = redirectType;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Integer getItemNum() {
		return itemNum;
	}

	public void setItemNum(Integer itemNum) {
		this.itemNum = itemNum;
	}
}