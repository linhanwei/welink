package com.welink.biz.common.model;

/**
 * Created by XUTIAN on 2015/3/7.
 */
public class CouponViewDO {
    //user_coupon表id
    private long userCouponId;
    //优惠券类型
    private int couponType;
    //可用的截至日期
    private long endTime;
    //优惠券金额
    private int value;
    //优惠券名称
    private String name;
    //使用规则
    private String description;
    //最小使用金额
    private int minValue;
    //是否已使用
    private boolean hasBeenUsed;
    //是否过期：false表示已过期
    private boolean isValid;
    
    private String picUrl;

    public boolean isHasBeenUsed() {
        return hasBeenUsed;
    }

    public void setHasBeenUsed(boolean hasBeenUsed) {
        this.hasBeenUsed = hasBeenUsed;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public long getUserCouponId() {
        return userCouponId;
    }

    public void setUserCouponId(long userCouponId) {
        this.userCouponId = userCouponId;
    }

    public int getCouponType() {
        return couponType;
    }

    public void setCouponType(int couponType) {
        this.couponType = couponType;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
}
