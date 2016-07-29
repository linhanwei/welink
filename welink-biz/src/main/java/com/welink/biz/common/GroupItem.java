package com.welink.biz.common;

import com.welink.commons.domain.Item;

/**
 * Created by saarixx on 27/11/14.
 */
public class GroupItem {
    private Item item;
    private String title;
    private long purchasingPrice;
    private long referencePrice;
    private long grouponPrice;
    private long onlineEndTime;
    private long onlineStartTime;
    private long quantity;
    private long type;
    private byte status;
    private long soldQuantity;
    private long shopId;
    private String bannerUrl;
    private long grouponId;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPurchasingPrice() {
        return purchasingPrice;
    }

    public void setPurchasingPrice(long purchasingPrice) {
        this.purchasingPrice = purchasingPrice;
    }

    public long getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(long referencePrice) {
        this.referencePrice = referencePrice;
    }

    public long getGrouponPrice() {
        return grouponPrice;
    }

    public void setGrouponPrice(long grouponPrice) {
        this.grouponPrice = grouponPrice;
    }

    public long getOnlineEndTime() {
        return onlineEndTime;
    }

    public void setOnlineEndTime(long onlineEndTime) {
        this.onlineEndTime = onlineEndTime;
    }

    public long getOnlineStartTime() {
        return onlineStartTime;
    }

    public void setOnlineStartTime(long onlineStartTime) {
        this.onlineStartTime = onlineStartTime;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(long soldQuantity) {
        this.soldQuantity = soldQuantity;
    }

    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public long getGrouponId() {
        return grouponId;
    }

    public void setGrouponId(long grouponId) {
        this.grouponId = grouponId;
    }

    @Override
    public String toString() {
        return "GroupItem{" +
                "item=" + item +
                ", title='" + title + '\'' +
                ", purchasingPrice=" + purchasingPrice +
                ", referencePrice=" + referencePrice +
                ", grouponPrice=" + grouponPrice +
                ", onlineEndTime=" + onlineEndTime +
                ", onlineStartTime=" + onlineStartTime +
                ", quantity=" + quantity +
                ", type=" + type +
                ", status=" + status +
                ", soldQuantity=" + soldQuantity +
                ", shopId=" + shopId +
                ", bannerUrl='" + bannerUrl + '\'' +
                ", grouponId=" + grouponId +
                '}';
    }
}
