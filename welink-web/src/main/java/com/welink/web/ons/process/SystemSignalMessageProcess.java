package com.welink.web.ons.process;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.welink.commons.Env;
import com.welink.web.ons.*;
import com.welink.web.ons.config.AliKeys;
import com.welink.web.ons.config.ONSPublish;
import com.welink.web.ons.config.ONSSubscribe;
import com.welink.web.ons.config.ONSTopic;
import com.welink.web.search.index.item.ItemIndexService;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by saarixx on 13/12/14.
 */
@Service
public class SystemSignalMessageProcess {

    static Logger logger = LoggerFactory.getLogger(SystemSignalMessageProcess.class);

    @Resource
    private MessageProcessFacade messageProcessFacade;

    @Resource
    private Env env;

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private ItemIndexService itemIndexService;

    @PostConstruct
    public void init() {

        if (env.isProd()) { // prod
            MessageProcess messageProcess = MessageProcess.newBuilder() //
            		.setAccessKey(AliKeys.ACCESS_KEY.toString())
            		.setSecretKey(AliKeys.SECRET_KEY.toString()) //
                    .setTopic(ONSTopic.SYSTEM_SIGNAL.toString()) //
                    .setProducerId(ONSPublish.SYSTEM_SIGNAL.toString()) //
                    .setConsumerId(ONSSubscribe.SYSTEM_SIGNAL.toString()) //
                    .setConsumeProcess(new ConsumeProcess() {
                        @Override
                        public void consume(Message message, ConsumeContext consumeContext) {
                            handle(message);
                        }
                    }) //
                    .build();

            messageProcessFacade.register(messageProcess);
        } else {
//            MessageProcess messageProcess = MessageProcess.newBuilder() //
//                    .setTopic(ONSTopic.SYSTEM_SIGNAL_TEST.toString()) //
//                    .setProducerId("PID2218978803-105") //
//                    .setConsumerId("CID2218978803-105") //
//                    .setConsumeProcess(new ConsumeProcess() {
//                        @Override
//                        public void consume(Message message, ConsumeContext consumeContext) {
//                            handle(message);
//                        }
//                    }) //
//                    .build();
//
//            messageProcessFacade.register(messageProcess);
        	MessageProcess messageProcess = MessageProcess.newBuilder() //
        			.setAccessKey(AliKeys.ACCESS_KEY_TEST.toString())
            		.setSecretKey(AliKeys.SECRET_KEY_TEST.toString()) //
                    .setTopic(ONSTopic.SYSTEM_SIGNAL_TEST.toString()) //
                    .setProducerId(ONSPublish.SYSTEM_SIGNAL_TEST.toString()) //
                    .setConsumerId(ONSSubscribe.SYSTEM_SIGNAL_TEST.toString()) //
                    .setConsumeProcess(new ConsumeProcess() {
                        @Override
                        public void consume(Message message, ConsumeContext consumeContext) {
                            handle(message);
                        }
                    }) //
                    .build();

            messageProcessFacade.register(messageProcess);
        }

    }

    public void handle(Message message) {
        SystemSignal systemSignal = JSON.parseObject(new String(message.getBody()), SystemSignal.class);

        logger.info("SYSTEM SIGNAL ARRIVAL --->>>> {}", systemSignal.getCode());

        String key = SystemSignalConstants.SYSTEM_SIGNAL_PREFIX + systemSignal.getCode();

        if (systemSignal.getCode().equals(SystemSignalConstants.Signal.SEARCH_ITEM_REFRESH.getCode())) {
            long value = memcachedClient.incr(key, 1, 1L, 10);

            logger.info("memcached code {} value {}", SystemSignalConstants.Signal.SEARCH_ITEM_REFRESH.getCode(), value);
            if (value == 1L) {
                logger.info("ready to refresh all item data ...");
                itemIndexService.refreshAll();
            }
        }
    }
}
