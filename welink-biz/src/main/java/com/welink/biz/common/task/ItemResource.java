package com.welink.biz.common.task;

import com.welink.biz.common.model.GrouponItemModel;
import com.welink.biz.common.model.ItemStatusModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by daniel on 14-11-20.
 */
public class ItemResource {

    public static Map<Long, ItemStatusModel> itemStatusModelMap = new ConcurrentHashMap<>();

    public static Map<Long, GrouponItemModel> grouponItemModelMap = new ConcurrentHashMap<Long, GrouponItemModel>();

}
