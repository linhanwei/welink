/**
 * Project Name:welink-web
 * File Name:ONSPublish.java
 * Package Name:com.welink.web.ons.config
 * Date:2015年11月2日下午11:20:20
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.ons.config;
/**
 * ClassName:ONSPublish <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月2日 下午11:20:20 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public enum ONSPublish {
	ITEM_UPDATE("PID_MIKU_ITEM_UPDATE"),

    ITEM_UPDATE_TEST("PID_MIKUTEST_ITEM_UPDATE"),

    SYSTEM_SIGNAL("PID_MIKU_SYSTEM_SIGNAL"),

    SYSTEM_SIGNAL_TEST("PID_MIKUTEST_SYSTEM_SIGNAL"),

    TRADE_EVENT("PID_MIKU_TRADE_EVENT"),

    TRADE_EVENT_TEST("PID_MIKUTEST_TRADE_EVENT"),
	;
	private String publish;

	ONSPublish(String publish) {
		this.publish = publish;
	}

	@Override
    public String toString() {
        return publish;
    }

}

