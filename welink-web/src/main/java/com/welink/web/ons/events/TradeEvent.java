package com.welink.web.ons.events;

import com.google.common.collect.Maps;

import java.util.Date;
import java.util.Map;

/**
 * Created by saarixx on 12/3/15.
 */
public class TradeEvent {

    /**
     * 订单编号，是tradeId这个字段
     */
    private Long tid;

    /**
     * trade 类型
     */
    private String topic;

    /**
     * 时间
     */
    private Date created;

    /**
     * 其他数据
     */
    private Map<String, Object> context = Maps.newHashMap();


    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "TradeEvent{" +
                "tid=" + tid +
                ", topic='" + topic + '\'' +
                ", created=" + created +
                ", context=" + context +
                '}';
    }
}
