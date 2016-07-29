package com.welink.commons.vo;

import java.math.BigInteger;

/**
 * Created by daniel on 15-4-1.
 */
public class TagViewVO {
    long id;
    String name;
    String kv;
    String pic;
    int weight;
    int type;
    byte status;
    String okv;
    BigInteger bit;

    public BigInteger getBit() {
        return bit;
    }

    public void setBit(BigInteger bit) {
        this.bit = bit;
    }

    public String getOkv() {
        return okv;
    }

    public void setOkv(String okv) {
        this.okv = okv;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKv() {
        return kv;
    }

    public void setKv(String kv) {
        this.kv = kv;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }
}
