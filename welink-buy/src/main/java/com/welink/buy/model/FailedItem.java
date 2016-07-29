package com.welink.buy.model;

/**
 * Created by daniel on 15-4-8.
 */
public class FailedItem {
    Long itemId;
    Byte approveStatus;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Byte getApproveStatus() {
        return approveStatus;
    }

    public void setApproveStatus(Byte approveStatus) {
        this.approveStatus = approveStatus;
    }
}
