package com.welink.biz.common.model;

/**
 * Created by daniel on 14-9-28.
 */
public class PushDO {

    String alert;

    String action;

    String badge = "Increment";

    String title = "";

    String sound = "cheering.caf";

    PushParam p;

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public PushParam getP() {
        return p;
    }

    public void setP(PushParam p) {
        this.p = p;
    }
}
