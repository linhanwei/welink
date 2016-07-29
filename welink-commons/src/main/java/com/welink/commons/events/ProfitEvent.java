package com.welink.commons.events;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.welink.commons.utils.NoNullFieldStringStyle;


/**
 * Created by saarixx on 9/1/15.
 */
public class ProfitEvent {

	private Date eventCreated = new Date();
    private Long tradeId;
    private Integer type;	//1=加分润；2=减分润

	public ProfitEvent(Long tradeId, Integer type) {
		super();
		this.tradeId = tradeId;
		this.type = type;
	}
	
    public Date getEventCreated() {
        return eventCreated;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, new NoNullFieldStringStyle());
    }

	public Long getTradeId() {
		return tradeId;
	}

	public Integer getType() {
		return type;
	}
	
}
