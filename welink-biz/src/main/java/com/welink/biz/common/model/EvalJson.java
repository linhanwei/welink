package com.welink.biz.common.model;

/**
 * Created by daniel on 14-11-14.
 */
public class EvalJson {

    private byte evalCode = 5;

    private long tradeId;

    private long itemId;

    private String pics;

    private String content;

    public byte getEvalCode() {
        return evalCode;
    }

    public void setEvalCode(byte evalCode) {
        this.evalCode = evalCode;
    }

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getPics() {
        return pics;
    }

    public void setPics(String pics) {
        this.pics = pics;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
