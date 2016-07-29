package com.welink.web.ons;

import com.alibaba.fastjson.JSON;
import com.welink.commons.Env;
import com.welink.web.ons.config.ONSTopic;
import com.welink.web.ons.events.TradeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by spider on 15/7/13.
 */
@Service("tradeMessageProcess")
public class TradeMessageProcessFacade {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(TradeMessageProcessFacade.class);

    @Resource
    private MessageProcessFacade messageProcessFacade;

    @Resource
    private Env env;

    public void sendMessage(long tradeId, String type) {

        TradeEvent tradeEvent = new TradeEvent();
        tradeEvent.setTid(tradeId);
        tradeEvent.setCreated(new Date());
        tradeEvent.setTopic(type);

        try {
            String message = JSON.toJSONString(tradeEvent);
            if (env.isProd()) {
                messageProcessFacade.sendMessage(ONSTopic.TRADE_EVENT.toString(), "trade", String.valueOf(tradeId), message);
            } else {
                messageProcessFacade.sendMessage(ONSTopic.TRADE_EVENT_TEST.toString(), "trade", String.valueOf(tradeId), message);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
