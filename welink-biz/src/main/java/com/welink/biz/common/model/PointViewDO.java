package com.welink.biz.common.model;

/**
 * Created by XUTIAN on 2015/3/7.
 */
public class PointViewDO {
    //积分变化情况(＋5表示新增5积分， －5表示使用5积分)
    private long score;
    //积分变化原因
    private String reason;
    //积分变化类型
    private int type;
    //积分变化时间
    private long createTime;
    //正负号:1（+）-1(-)
    private int symbol;

    public int getSymbol() {
        return symbol;
    }

    public void setSymbol(int symbol) {
        this.symbol = symbol;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
