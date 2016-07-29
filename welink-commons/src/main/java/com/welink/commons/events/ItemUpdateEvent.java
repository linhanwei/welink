package com.welink.commons.events;

import org.apache.ibatis.mapping.SqlCommandType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * item的更新，id商品id，type商品更新类型
 * <p/>
 * Created by saarixx on 10/12/14.
 */
public class ItemUpdateEvent extends BaseEvent {

    /**
     * 更新的开始时间
     */
    private Long lastUpdatedStart;

    /**
     * 更新的结束时间
     */
    private Long lastUpdatedEnd;

    /**
     * 更新类型
     */
    private SqlCommandType type;

    public ItemUpdateEvent(Long lastUpdatedStart, Long lastUpdatedEnd, SqlCommandType type) {
        checkNotNull(lastUpdatedStart);
        checkNotNull(lastUpdatedEnd);
        checkNotNull(type);
        this.lastUpdatedStart = lastUpdatedStart;
        this.lastUpdatedEnd = lastUpdatedEnd;
        this.type = type;
    }

    public Long getLastUpdatedStart() {
        return lastUpdatedStart;
    }

    public Long getLastUpdatedEnd() {
        return lastUpdatedEnd;
    }

    public SqlCommandType getType() {
        return type;
    }
}
