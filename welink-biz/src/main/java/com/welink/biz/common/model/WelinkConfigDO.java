package com.welink.biz.common.model;

/**
 * Created by daniel on 14-10-31.
 */
public class WelinkConfigDO {

    private boolean hot_line = true;

    private boolean announce = true;

    private boolean express = true;

    private boolean complain = true;

    private boolean property_management_fee = true;

    private boolean household_management = true;

    private boolean maintenance = true;

    private boolean bottled_water = true;

    public boolean isHot_line() {
        return hot_line;
    }

    public void setHot_line(boolean hot_line) {
        this.hot_line = hot_line;
    }

    public boolean isAnnounce() {
        return announce;
    }

    public void setAnnounce(boolean announce) {
        this.announce = announce;
    }

    public boolean isExpress() {
        return express;
    }

    public void setExpress(boolean express) {
        this.express = express;
    }

    public boolean isComplain() {
        return complain;
    }

    public void setComplain(boolean complain) {
        this.complain = complain;
    }

    public boolean isProperty_management_fee() {
        return property_management_fee;
    }

    public void setProperty_management_fee(boolean property_management_fee) {
        this.property_management_fee = property_management_fee;
    }

    public boolean isHousehold_management() {
        return household_management;
    }

    public void setHousehold_management(boolean household_management) {
        this.household_management = household_management;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public boolean isBottled_water() {
        return bottled_water;
    }

    public void setBottled_water(boolean bottled_water) {
        this.bottled_water = bottled_water;
    }
}
