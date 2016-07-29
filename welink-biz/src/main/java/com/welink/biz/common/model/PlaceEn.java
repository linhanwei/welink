package com.welink.biz.common.model;

/**
 * Created by daniel on 14-10-15.
 */
public class PlaceEn {
    String name;
    String telephone;
    Location location;
    String address;
    String Street_id;
    String uid;
    DetailInfo detail_info;

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStreet_id() {
        return Street_id;
    }

    public void setStreet_id(String street_id) {
        Street_id = street_id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public DetailInfo getDetail_info() {
        return detail_info;
    }

    public void setDetail_info(DetailInfo detail_info) {
        this.detail_info = detail_info;
    }
}
