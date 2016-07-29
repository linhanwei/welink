package com.welink.web.ons;

import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;

/**
 * Created by saarixx on 12/12/14.
 */
public interface ConsumeProcess {

    public void consume(Message message, ConsumeContext  consumeContext);
}
