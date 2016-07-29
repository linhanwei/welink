/**
 * Project Name:welink-commons
 * File Name:UserInteractionRequestEvent.java
 * Package Name:com.welink.commons.events
 * Date:2016年1月15日下午5:02:19
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.promotion.event;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.welink.commons.utils.NoNullFieldStringStyle;
import com.welink.promotion.reactive.UserInteractionRequest;

/**
 * ClassName:UserInteractionRequestEvent <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月15日 下午5:02:19 <br/>
 * @author   LuoGuangChun
 */
public class UserInteractionRequestEvent {
	private Date eventCreated = new Date();
	private UserInteractionRequest userInteractionRequest;
	
	public UserInteractionRequestEvent(
			UserInteractionRequest userInteractionRequest) {
		checkNotNull(userInteractionRequest);
		this.userInteractionRequest = userInteractionRequest;
	}

	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, new NoNullFieldStringStyle());
    }
	
	public Date getEventCreated() {
		return eventCreated;
	}
	public UserInteractionRequest getUserInteractionRequest() {
		return userInteractionRequest;
	}
}

