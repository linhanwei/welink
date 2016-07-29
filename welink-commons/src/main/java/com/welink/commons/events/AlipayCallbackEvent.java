package com.welink.commons.events;

import com.welink.commons.domain.AlipayBackDO;

/**
 * Created by saarixx on 9/1/15.
 */
public class AlipayCallbackEvent extends BaseEvent {

    private AlipayBackDO alipayBackDO;

    public AlipayCallbackEvent(AlipayBackDO alipayBackDO) {
        this.alipayBackDO = alipayBackDO;
    }

    public AlipayBackDO getAlipayBackDO() {
        return alipayBackDO;
    }
}
