package com.welink.commons.events;

import com.welink.commons.utils.NoNullFieldStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * Created by saarixx on 9/1/15.
 */
public class BaseEvent {

    private Date eventCreated = new Date();

    public Date getEventCreated() {
        return eventCreated;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, new NoNullFieldStringStyle());
    }
}
