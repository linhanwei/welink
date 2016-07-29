package com.welink.biz.common.model;

/**
 * Created by daniel on 14-9-11.
 */
public class UserBaseProfile {

    boolean isProprietor = false;

    boolean hasProfile = false;

    long profileId = -1l;

    public boolean isProprietor() {
        return isProprietor;
    }

    public void setProprietor(boolean isProprietor) {
        this.isProprietor = isProprietor;
    }

    public boolean isHasProfile() {
        return hasProfile;
    }

    public void setHasProfile(boolean hasProfile) {
        this.hasProfile = hasProfile;
    }

    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }
}
