/**
 * Project Name:welink-web
 * File Name:ONSSubscribe.java
 * Package Name:com.welink.web.ons.config
 * Date:2015年11月2日下午11:20:49
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.ons.config;
/**
 * ClassName:ONSSubscribe <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月2日 下午11:20:49 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public enum ONSSubscribe {
	ITEM_UPDATE("CID_MIKU_ITEM_UPDATE"),

    ITEM_UPDATE_TEST("CID_MIKUTEST_ITEM_UPDATE"),

    SYSTEM_SIGNAL("CID_MIKU_SYSTEM_SIGNAL"),

    SYSTEM_SIGNAL_TEST("CID_MIKUTEST_SYSTEM_SIGNAL"),

    TRADE_EVENT("CID_MIKU_TRADE_EVENT"),

    TRADE_EVENT_TEST("CID_MIKUTEST_TRADE_EVENT"),
	;
	
	private String subscribe;


	ONSSubscribe(String subscribe) {
		this.subscribe = subscribe;
	}


	@Override
    public String toString() {
        return subscribe;
    }
	
}
