package com.welink.biz.common.model;

import com.welink.commons.domain.ProfileExtDO;

/**
 * Created by daniel on 14-9-24.
 */
public class ProfileDOExtT {

    private ProfileExtDO profileExtDO = new ProfileExtDO();

    boolean changed = false;

    public ProfileExtDO getProfileExtDO() {
        return profileExtDO;
    }

    public void setProfileExtDO(ProfileExtDO profileExtDO) {
        this.profileExtDO = profileExtDO;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
