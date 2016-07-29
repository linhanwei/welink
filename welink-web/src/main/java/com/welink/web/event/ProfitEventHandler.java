/**
 * Project Name:welink-web
 * File Name:ProfitEventHandler.java
 * Package Name:com.welink.web.event
 * Date:2015年11月6日下午4:54:08
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.event;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.welink.biz.profit.AddProfitImpl;
import com.welink.biz.profit.CutProfitImpl;
import com.welink.biz.service.ProfitService;
import com.welink.commons.events.ProfitEvent;

/**
 * ClassName:ProfitEventHandler <br/>
 * Function: 分润事件处理 <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月6日 下午4:54:08 <br/>
 * @author   LuoGuangChun
 */
@Service
public class ProfitEventHandler {
	static Logger logger = LoggerFactory.getLogger(ProfitEventHandler.class);

    @Resource
    private AsyncEventBus asyncEventBus;

    @Resource
    private ProfitService profitService;
    
    @Resource
    private AddProfitImpl addProfitImpl;
    
    @Resource
    private CutProfitImpl cutProfitImpl;


    @Subscribe
    public void handle(ProfitEvent profitEvent) throws InterruptedException {
        logger.info("the async event has been delayed {}ms", (System.currentTimeMillis() - profitEvent.getEventCreated().getTime()));
        //1=加分润；2=减分润
        if(null != profitEvent && profitEvent.getType().equals(1)){
        	//加分润
        	addProfitImpl.handle(profitEvent.getTradeId());
        }else if(null != profitEvent && profitEvent.getType().equals(2)){
        	//减分润
        	cutProfitImpl.handle(profitEvent.getTradeId());    //orderId
        }
        //profitService.profitByTradeId(profitEvent.getTradeId());		//分润处理
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

