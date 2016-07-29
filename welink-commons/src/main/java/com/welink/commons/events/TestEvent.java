package com.welink.commons.events;


/**
 * Created by saarixx on 4/2/15.
 */
public class TestEvent extends BaseEvent {

    private String name;

    public TestEvent(String name) {
        this.name = name;
    }

	public String getName() {
		return name;
	}

}
