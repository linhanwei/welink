package com.welink.web.ons.config;

/**
 * Created by saarixx on 11/12/14.
 */
public enum ONSTopic {

    /*ITEM_UPDATE("unescn_item_update"),

    ITEM_UPDATE_TEST("unescn_item_update"),

    SYSTEM_SIGNAL("unescn_system_signal"),

    SYSTEM_SIGNAL_TEST("unescn_system_signal"),

    TRADE_EVENT("unescn_trade_event"),

    TRADE_EVENT_TEST("unescn_trade_event"),*/
	
	ITEM_UPDATE("miku_item_update"),

    ITEM_UPDATE_TEST("mikutest_item_update"),

    SYSTEM_SIGNAL("miku_system_signal"),

    SYSTEM_SIGNAL_TEST("mikutest_system_signal"),

    TRADE_EVENT("miku_trade_event"),

    TRADE_EVENT_TEST("mikutest_trade_event"),


    // ------------------------------------------------
    ;

    private String topic;

    ONSTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return topic;
    }
}
