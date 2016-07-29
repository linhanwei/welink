package com.welink.web.event;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.welink.commons.Env;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.events.ItemUpdateEvent;
import com.welink.commons.persistence.ItemMapper;
import com.welink.web.ons.MessageProcessFacade;
import com.welink.web.ons.config.ONSTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 如果商品有变化，通过EventBus异步到这边处理
 * 处理逻辑就是把变化的商品作为消息，发送给阿里云ONS
 * <p/>
 * Created by saarixx on 10/12/14.
 */
@Service
public class ItemUpdateEventHandler {

    static Logger logger = LoggerFactory.getLogger(ItemUpdateEventHandler.class);

    @Resource
    private AsyncEventBus asyncEventBus;

    @Resource
    private MessageProcessFacade messageProcessFacade;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private Env env;


    @Subscribe
    public void handle(ItemUpdateEvent itemUpdateEvent) {

        logger.info("the async event has been delayed {}ms", (System.currentTimeMillis() - itemUpdateEvent.getEventCreated().getTime()));

        long start = itemUpdateEvent.getLastUpdatedStart();
        Long end = itemUpdateEvent.getLastUpdatedEnd();

        String type = itemUpdateEvent.getType().toString();

        ItemExample itemExample = new ItemExample();
        itemExample.createCriteria().andLastUpdatedBetween(new Date(start), new Date(end));

        List<Item> itemList = itemMapper.selectByExample(itemExample);

        for (Item item : itemList) {
            // 消息发送到阿里云的ONS
            Map<String, String> params = Maps.newHashMap();
            params.put("item_id", String.valueOf(item.getId()));
            params.put("type", type);

            String message = JSON.toJSONString(params);

            if (env.isProd()) {
                messageProcessFacade.sendMessage(ONSTopic.ITEM_UPDATE.toString(), "item", String.valueOf(item.getId()), message);
            } else {
                messageProcessFacade.sendMessage(ONSTopic.ITEM_UPDATE_TEST.toString(), "item", String.valueOf(item.getId()), message);
            }
        }
    }

    @PostConstruct
    public void init() {
        asyncEventBus.register(this);
    }

    @PreDestroy
    public void destroy() {
        asyncEventBus.unregister(this);
    }
}
