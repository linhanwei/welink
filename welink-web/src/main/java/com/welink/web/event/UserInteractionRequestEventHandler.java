/**
 * Project Name:welink-web
 * File Name:ProfitEventHandler.java
 * Package Name:com.welink.web.event
 * Date:2015年11月6日下午4:54:08
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.event;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.welink.biz.service.ProfitService;
import com.welink.commons.domain.CouponDO;
import com.welink.commons.events.ProfitEvent;
import com.welink.promotion.event.UserInteractionRequestEvent;
import com.welink.promotion.reactive.UserInteractionEffect;

/**
 * ClassName:ProfitEventHandler <br/>
 * Function: 分润事件处理 <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月6日 下午4:54:08 <br/>
 * @author   LuoGuangChun
 */
@Service
public class UserInteractionRequestEventHandler {
	static Logger logger = LoggerFactory.getLogger(UserInteractionRequestEventHandler.class);

    @Resource
    private AsyncEventBus asyncEventBus;

    @Resource
    private UserInteractionEffect userInteractionEffect;
    
    


    @Subscribe
    public void handle(UserInteractionRequestEvent UserInteractionRequestEvent) throws InterruptedException {
        logger.info("the async event has been delayed {}ms", (System.currentTimeMillis() - UserInteractionRequestEvent.getEventCreated().getTime()));
        try {
        	/*System.out.println("UserInteraction-------------------------------------------------------");
        	System.out.println("---------------------------------------Type: "+UserInteractionRequestEvent.getUserInteractionRequest().getType());
        	List<CouponDO> couponDOs = (List<CouponDO>) UserInteractionRequestEvent.getUserInteractionRequest().getParams().get("couponDOs");
        	for(CouponDO couponDO : couponDOs){
        		System.out.println("..................."+couponDO.getName());
        	}*/
        	
        	userInteractionEffect.interactive(UserInteractionRequestEvent.getUserInteractionRequest());	//用户交互处理，如积分优惠券
		} catch (Exception e) {
			e.printStackTrace();
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

