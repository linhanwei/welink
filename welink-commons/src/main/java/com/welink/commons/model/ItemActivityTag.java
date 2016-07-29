package com.welink.commons.model;

/**
 * 
 * ClassName: ItemActivityTag <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2015年12月9日 下午1:37:01 <br/>
 *
 * @author LuoGuangChun
 */
public class ItemActivityTag {

	public Long activityPrice;		//活动价格
	public Integer baseSoldNum;		//购买基数
	public Integer multiple;		//倍数
	
	public Long getActivityPrice() {
		return activityPrice;
	}
	public void setActivityPrice(Long activityPrice) {
		this.activityPrice = activityPrice;
	}
	public Integer getBaseSoldNum() {
		return baseSoldNum;
	}
	public void setBaseSoldNum(Integer baseSoldNum) {
		this.baseSoldNum = baseSoldNum;
	}
	public Integer getMultiple() {
		return multiple;
	}
	public void setMultiple(Integer multiple) {
		this.multiple = multiple;
	}
}
