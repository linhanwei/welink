package com.welink.biz.common.model;

/**
 * Created by daniel on 15-4-27.
 */
public class AnnouceViewDO {
    Long communityId;
    String content;

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
