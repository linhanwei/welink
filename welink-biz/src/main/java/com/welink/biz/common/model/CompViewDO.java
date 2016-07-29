package com.welink.biz.common.model;

/**
 * Created by daniel on 15-4-27.
 */
public class CompViewDO {
    Byte status;
    String title;
    Long start;
    Long end;
    Long cp_id;
    String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getCp_id() {
        return cp_id;
    }

    public void setCp_id(Long cp_id) {
        this.cp_id = cp_id;
    }
}
