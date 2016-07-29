package com.welink.web.ons.process;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.google.common.base.Optional;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.domain.UserInteractionRecordsDOExample;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.promotion.PromotionType;
import com.welink.promotion.reactive.PromotionResult;
import com.welink.promotion.reactive.UserInteractionEffect;
import com.welink.promotion.reactive.UserInteractionRequest;
import com.welink.web.ons.ConsumeProcess;
import com.welink.web.ons.MessageProcess;
import com.welink.web.ons.MessageProcessFacade;
import com.welink.web.ons.config.AliKeys;
import com.welink.web.ons.config.ONSPublish;
import com.welink.web.ons.config.ONSSubscribe;
import com.welink.web.ons.config.ONSTopic;
import com.welink.web.ons.events.TradeEvent;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by saarixx on 12/3/15.
 */
@Service
public class TradeEventMessageProcess {

    static Logger logger = LoggerFactory.getLogger(TradeEventMessageProcess.class);

    @Resource
    private MessageProcessFacade messageProcessFacade;

    @Resource
    private Env env;

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private UserInteractionEffect userInteractionEffect;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;

    @PostConstruct
    public void init() {

        if (env.isProd()) { // prod
            MessageProcess messageProcess = MessageProcess.newBuilder() //
            		.setAccessKey(AliKeys.ACCESS_KEY.toString())
            		.setSecretKey(AliKeys.SECRET_KEY.toString()) //
                    .setTopic(ONSTopic.TRADE_EVENT.toString()) //
                    .setProducerId(ONSPublish.TRADE_EVENT.toString()) //
                    .setConsumerId(ONSSubscribe.TRADE_EVENT.toString()) //
                    .setConsumeProcess(new ConsumeProcess() {
                        @Override
                        public void consume(Message message, ConsumeContext consumeContext) {
                            handle(message, consumeContext);
                        }
                    }) //
                    .build();

            messageProcessFacade.register(messageProcess);
        } else {
//            MessageProcess messageProcess = MessageProcess.newBuilder() //
//                    .setTopic(ONSTopic.TRADE_EVENT_TEST.toString()) //
//                    .setProducerId("PID_2218978803-107") //
//                    .setConsumerId("CID_2218978803-107") //
//                    .setConsumeProcess(new ConsumeProcess() {
//                        @Override
//                        public void consume(Message message, ConsumeContext consumeContext) {
//                            handle(message, consumeContext);
//                        }
//                    }) //
//                    .build();
//
//            messageProcessFacade.register(messageProcess);
        	MessageProcess messageProcess = MessageProcess.newBuilder() //
        			.setAccessKey(AliKeys.ACCESS_KEY_TEST.toString())
            		.setSecretKey(AliKeys.SECRET_KEY_TEST.toString()) //
                    .setTopic(ONSTopic.TRADE_EVENT_TEST.toString()) //
                    .setProducerId(ONSPublish.TRADE_EVENT_TEST.toString()) //
                    .setConsumerId(ONSSubscribe.TRADE_EVENT_TEST.toString()) //
                    .setConsumeProcess(new ConsumeProcess() {
                        @Override
                        public void consume(Message message, ConsumeContext consumeContext) {
                            handle(message, consumeContext);
                        }
                    }) //
                    .build();

            messageProcessFacade.register(messageProcess);
        }

    }

    public boolean pointAcquiredByTradeSuccess(Trade trade) {
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.createCriteria() //
                .andUserIdEqualTo(checkNotNull(trade.getBuyerId())) //
                .andTypeEqualTo(PromotionType.POINT_TRADE_SUCCESS.getCode()) //
                .andTargetIdEqualTo(String.valueOf(checkNotNull(trade.getTradeId())));

        List<UserInteractionRecordsDO> userInteractionRecordsDOs = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);

        if (userInteractionRecordsDOs.isEmpty()) {
            // 并没有因为这次交易获得积分
            UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
            userInteractionRequest.setUserId(checkNotNull(trade.getBuyerId(), "buyer_id should not be null. "));
            userInteractionRequest.setType(PromotionType.POINT_TRADE_SUCCESS.getCode());
            userInteractionRequest.setTargetId(checkNotNull(String.valueOf(trade.getTradeId()), "tradeId should not be null. "));
            userInteractionRequest.setValue(checkNotNull(trade.getTotalFee(), "payment should not be null. "));

            Optional<PromotionResult> promotionResultOptional = userInteractionEffect.interactive(userInteractionRequest);
            if (promotionResultOptional.isPresent()) {
                PromotionResult promotionResult = promotionResultOptional.get();
                return promotionResult.getReward();
            }
        }

        return false;
    }

    private void handle(Message message, ConsumeContext consumeContext) {
        TradeEvent tradeEvent = JSON.parseObject(new String(message.getBody()), TradeEvent.class);

        long tradeId = tradeEvent.getTid();

        if ("TradeSuccess".equalsIgnoreCase(tradeEvent.getTopic())) {

            TradeExample tradeExample = new TradeExample();
            tradeExample.createCriteria() //
                    .andTradeIdEqualTo(tradeId);

            List<Trade> trades = tradeMapper.selectByExample(tradeExample);

            if (!trades.isEmpty()) {
                Trade trade = trades.get(0);
                // trade 状态变化，如果是交易成功，开始给积分
                if (trade.getStatus() == Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId()) {
                    pointAcquiredByTradeSuccess(trade);
                }
            }

        }

    }
}
