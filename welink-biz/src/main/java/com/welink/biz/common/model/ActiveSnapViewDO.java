package com.welink.biz.common.model;

/**
 * Created by daniel on 15-1-14.
 */
public class ActiveSnapViewDO {
    String pics;//商品主图图片
    String title;//商品标题
    long itemId;//id
    long refPrice;//商品参考价格
    String specification;//商品规格
    long realNum;//库存
    long activeId;
    int activeStatus;
    long activePrice;//活动价格
    long bannerId;
    long endTime;
    long inventory;
    int limitNum;
    long startTime;

    ItemViewDO itemViewDO;

    public ItemViewDO getItemViewDO() {
        return itemViewDO;
    }

    public void setItemViewDO(ItemViewDO itemViewDO) {
        this.itemViewDO = itemViewDO;
    }

    public long getRealNum() {
        return realNum;
    }

    public void setRealNum(long realNum) {
        this.realNum = realNum;
    }

    public String getPics() {
        return pics;
    }

    public void setPics(String pics) {
        this.pics = pics;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getRefPrice() {
        return refPrice;
    }

    public void setRefPrice(long refPrice) {
        this.refPrice = refPrice;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public long getActiveId() {
        return activeId;
    }

    public void setActiveId(long activeId) {
        this.activeId = activeId;
    }

    public int getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(int activeStatus) {
        this.activeStatus = activeStatus;
    }

    public long getActivePrice() {
        return activePrice;
    }

    public void setActivePrice(long activePrice) {
        this.activePrice = activePrice;
    }

    public long getBannerId() {
        return bannerId;
    }

    public void setBannerId(long bannerId) {
        this.bannerId = bannerId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getInventory() {
        return inventory;
    }

    public void setInventory(long inventory) {
        this.inventory = inventory;
    }

    public int getLimitNum() {
        return limitNum;
    }

    public void setLimitNum(int limitNum) {
        this.limitNum = limitNum;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
