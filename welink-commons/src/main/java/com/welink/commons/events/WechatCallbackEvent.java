package com.welink.commons.events;

import com.welink.commons.domain.WeiXinBackDO;

/**
 * Created by saarixx on 4/2/15.
 */
public class WechatCallbackEvent extends BaseEvent {

    private WeiXinBackDO weiXinBackDO;

    public WechatCallbackEvent(WeiXinBackDO weiXinBackDO) {
        this.weiXinBackDO = weiXinBackDO;
    }

    public WeiXinBackDO getWeiXinBackDO() {
        return weiXinBackDO;
    }
}
