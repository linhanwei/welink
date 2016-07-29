package com.welink.biz.common.model;

/**
 * Created by daniel on 14-11-22.
 */
public class AddressResult {
    private boolean update;
    String address = "";
    String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
