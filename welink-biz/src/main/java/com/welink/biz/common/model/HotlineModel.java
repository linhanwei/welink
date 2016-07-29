package com.welink.biz.common.model;

import com.welink.commons.domain.HotlineDO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 14-10-14.
 */
public class HotlineModel {

    private String title;

    private long parentId;

    private List<HotlineDO> hotlineDOs = new ArrayList<HotlineDO>();

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<HotlineDO> getHotlineDOs() {
        return hotlineDOs;
    }

    public void setHotlineDOs(List<HotlineDO> hotlineDOs) {
        this.hotlineDOs = hotlineDOs;
    }
}
