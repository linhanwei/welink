package com.welink.biz.common.model;

/**
 * Created by daniel on 15-4-7.
 */
public class ItemCanBuy {
    Long itemId;
    Integer cap;
    Integer realCap;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getCap() {
        return cap;
    }

    public void setCap(Integer cap) {
        this.cap = cap;
    }

    public Integer getRealCap() {
        return realCap;
    }

    public void setRealCap(Integer realCap) {
        this.realCap = realCap;
    }
}
