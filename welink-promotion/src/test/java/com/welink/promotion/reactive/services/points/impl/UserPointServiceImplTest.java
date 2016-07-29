package com.welink.promotion.reactive.services.points.impl;

import com.welink.commons.domain.Order;
import com.welink.commons.domain.PointAccountDO;
import com.welink.commons.domain.PointRecordDO;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.promotion.PromotionType;
import com.welink.promotion.reactive.PromotionResult;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class UserPointServiceImplTest {

    Logger logger = LoggerFactory.getLogger(UserPointServiceImplTest.class);

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testUpdateUserPoint() throws Exception {
        // load up the knowledge base
        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();

        KieSession kSession = kContainer.newKieSession("promotion-rules");

        try {

            PromotionResult promotionResult = new PromotionResult();


            PointAccountDO pointAccountDO = new PointAccountDO();
            UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
            userInteractionRecordsDO.setType(PromotionType.POINT_SIGN_IN.getCode());
            userInteractionRecordsDO.setDateCreated(new Date());

            PointRecordDO pointRecordDO = new PointRecordDO();

            kSession.insert(promotionResult);
            kSession.insert(pointAccountDO);
            kSession.insert(userInteractionRecordsDO);
            kSession.insert(pointRecordDO);
            kSession.addEventListener(new DebugRuleRuntimeEventListener());
            kSession.addEventListener(new DefaultAgendaEventListener() {
                public void afterMatchFired(AfterMatchFiredEvent event) {
                    super.afterMatchFired(event);
                    System.out.println(event);
                }
            });

            kSession.fireAllRules();

            logger.info(ToStringBuilder.reflectionToString(promotionResult));
            logger.info(ToStringBuilder.reflectionToString(pointRecordDO));
            logger.info(ToStringBuilder.reflectionToString(pointAccountDO));

        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        } finally {
            kSession.dispose();
        }
    }
    
    
    @Test
    public void testZp(){
    	
    	 KieServices ks = KieServices.Factory.get();
         KieContainer kContainer = ks.getKieClasspathContainer();
         KieSession kSession = kContainer.newKieSession("promotion-rules");
         try {
             PromotionResult promotionResult = new PromotionResult();
             UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
             //对应的订单  订单号  商品关联id 创建的时间    总净额
             Order order=new Order();
             //互动表信息表：用户id 活动类型   订单号  时间的修改
             userInteractionRecordsDO.setUserId(-1L);
             promotionResult.setCode(0);
             logger.info("=================");
             logger.info(ToStringBuilder.reflectionToString(promotionResult));
             logger.info(ToStringBuilder.reflectionToString(order));
             logger.info(ToStringBuilder.reflectionToString(userInteractionRecordsDO));
             logger.info("===============");
//             PromotionType.ITEM_ENOUGY_NUM_STATUS.getCode()
             kSession.insert(promotionResult);
             kSession.insert(userInteractionRecordsDO);
             kSession.insert(order);
             kSession.addEventListener(new DebugRuleRuntimeEventListener());
             kSession.addEventListener(new DefaultAgendaEventListener() {
                 public void afterMatchFired(AfterMatchFiredEvent event) {
                     super.afterMatchFired(event);
                     System.out.println(event);
                 }
             });

             kSession.fireAllRules();
             
             
             logger.info("*********************");
             logger.info(ToStringBuilder.reflectionToString(promotionResult));
             logger.info(ToStringBuilder.reflectionToString(order));
             logger.info(ToStringBuilder.reflectionToString(userInteractionRecordsDO));
             logger.info("*********************");

         } catch (Exception e) {
             logger.info(e.getMessage(), e);
         } finally {
             kSession.dispose();
         }
    }
}