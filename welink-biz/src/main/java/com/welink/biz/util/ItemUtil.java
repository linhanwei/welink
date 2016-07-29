package com.welink.biz.util;

import com.alibaba.fastjson.JSON;
import com.welink.commons.domain.Item;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by daniel on 15-1-26.
 */
public class ItemUtil {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ItemUtil.class);

    public long getReferencePrice(Item item) {
        if (null == item || StringUtils.isBlank(item.getFeatures())) {
            return -1;
        }
        try {
            Map map = (Map) JSON.parse(item.getFeatures());
            for (Object key : map.keySet()) {
                if (org.apache.commons.lang.StringUtils.equals("referencePrice", key.toString())) {
                    return Long.valueOf(map.get(key).toString());
                }
            }
        } catch (Exception e) {
            log.error("parse feature failed. itemId:" + item.getId());
        }
        return -1;
    }

    public long getPurchasingPrice(Item item) {
        if (null == item || StringUtils.isBlank(item.getFeatures())) {
            return -1;
        }
        try {
            Map map = (Map) JSON.parse(item.getFeatures());
            for (Object key : map.keySet()) {
                if (org.apache.commons.lang.StringUtils.equals("purchasingPrice", key.toString())) {
                    return Long.valueOf(map.get(key).toString());
                }
            }
        } catch (Exception e) {
            log.error("parse feature failed. itemId:" + item.getId());
        }
        return -1;
    }

    /**
     * 获取商品额外属性
     *
     * @param item
     * @return
     */
    public static Object getExtMap(Item item) {
        if (null == item || StringUtils.isBlank(item.getFeatures())) {
            return null;
        }
        String extStr = null;
        try {
            Map map = (Map) JSON.parse(item.getFeatures());
            for (Object key : map.keySet()) {
                if (org.apache.commons.lang.StringUtils.equals("ext", key.toString())) {
                    extStr = map.get(key).toString();
                    return JSON.parse(extStr);
                }
            }
        } catch (Exception e) {
            log.error("ext feature failed. itemId:" + item.getId());
        }
        return null;
    }

    /**
     * 获取商品是否含有某种特殊标记
     *
     * @param item
     * @return
     */
    public static boolean checkTag(Item item, String tag) {
        if (null == item || StringUtils.isBlank(item.getFeatures())) {
            return false;
        }
        String extStr = null;
        try {
            Map map = (Map) JSON.parse(item.getFeatures());
            for (Object key : map.keySet()) {
                if (org.apache.commons.lang.StringUtils.equals("tags", key.toString())) {
                    extStr = map.get(key).toString();
                    if (StringUtils.isBlank(extStr)) {
                        return false;
                    }
                    if (extStr.split(";").length > 0) {
                        for (String s : extStr.split(";")) {
                            if (StringUtils.equals(s, tag)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("ext feature failed. itemId:" + item.getId());
        }
        return false;
    }


    public static void main(String[] args) {
        Item item = new Item();
        String features = "{\"purchasingPrice\":410,\"referencePrice\":0,\"tags\":\"123;23\"}";
        item.setFeatures(features);
        boolean hasTag = checkTag(item, "2");
        System.out.println("----------" + hasTag + "-----------");
    }
}
