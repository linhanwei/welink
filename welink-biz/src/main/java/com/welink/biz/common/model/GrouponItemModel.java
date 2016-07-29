package com.welink.biz.common.model;

/**
 * Created by daniel on 14-11-20.
 */
public class GrouponItemModel {

    private long itemId;

    private long startTime;

    private long endTime;

    private byte status;

    private byte type;

    private long grouponId;

    public long getGrouponId() {
        return grouponId;
    }

    public void setGrouponId(long grouponId) {
        this.grouponId = grouponId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
