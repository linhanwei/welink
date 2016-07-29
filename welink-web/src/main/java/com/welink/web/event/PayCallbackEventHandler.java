package com.welink.web.event;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.welink.commons.domain.*;
import com.welink.commons.events.AlipayCallbackEvent;
import com.welink.commons.events.WechatCallbackEvent;
import com.welink.commons.persistence.*;
import com.welink.commons.tacker.KeenIOTracker;
import com.welink.commons.tacker.PayTrackObject;
import com.welink.web.common.filter.Profiler;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * 支付 callback 之后，会有一些统计信息做纪录，都放到了这个异步处理里面
 * <p/>
 * 最好保证所有的trade都是已付款状态
 * <p/>
 * Created by saarixx on 9/1/15.
 */
@Service
public class PayCallbackEventHandler {

    static Logger logger = LoggerFactory.getLogger(PayCallbackEventHandler.class);

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    private static final String TRADE_FINISH = "TRADE_FINISHED";

    public final static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    /*@Resource
    private KeenIOTracker keenIOTracker;*/

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private AlipayBackDOMapper alipayBackDOMapper;

    @Resource
    private WeiXinBackDOMapper weiXinBackDOMapper;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private AsyncEventBus asyncEventBus;

    @Subscribe
    public void handle(WechatCallbackEvent wechatCallbackEvent) {
        logger.info("the async event has been delayed {}ms", (System.currentTimeMillis() - wechatCallbackEvent.getEventCreated().getTime()));

        checkNotNull(wechatCallbackEvent);
        checkNotNull(wechatCallbackEvent.getWeiXinBackDO());

        WeiXinBackDOExample weiXinBackDOExample = new WeiXinBackDOExample();
        weiXinBackDOExample.createCriteria().andOutTradeNoEqualTo(wechatCallbackEvent.getWeiXinBackDO().getOutTradeNo());

        List<WeiXinBackDO> weiXinBackDOs = weiXinBackDOMapper.selectByExample(weiXinBackDOExample);

        int count = 0;

        for (WeiXinBackDO weiXinBackDO : weiXinBackDOs) {
            if (StringUtils.equalsIgnoreCase(weiXinBackDO.getReturnCode(), "SUCCESS") && StringUtils.equalsIgnoreCase(weiXinBackDO.getResultCode(), "SUCCESS")) {
                count++;
            }
        }

        if (count != 1) {
            logger.warn("the count of the wechat back do not equal 1, ignore ... the trade id is {}", wechatCallbackEvent.getWeiXinBackDO().getOutTradeNo());
            return;
        }

        logger.info("#### analytics tracker ####");
        Profiler.start("analytics tracker");

        try {
            WeiXinBackDO weiXinBackDO = wechatCallbackEvent.getWeiXinBackDO();

            Trade trade = queryRelativeTrade(weiXinBackDO.getOutTradeNo());

            List<Order> orders = queryOrdersSyncTradeStatus(trade);

            String mobile = queryBuyerMobile(trade);

            logger.info("#### keen io ####");
            Profiler.enter("keen io");

            //keenIOTracker.track(mobile, "pay-callback", transform(wechatCallbackEvent, trade), null);
            //keenIOTracker.track(mobile, "trade", trade, null);
            for (Order order : orders) {
                Map<String, Object> features = ImmutableMap.<String, Object>builder() //
                        .put("tradeId", trade.getId()) //
                        .put("payType", trade.getPayType())
                        .build();
                //keenIOTracker.track(mobile, "order", order, features);
            }

            logger.info("#### end ####");
            Profiler.release();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Profiler.release();

            long duration = Profiler.getDuration();

            logger.info("analytics post in {}ms\n{}\n",
                    duration, Profiler.dump("Detail: ", "        "));

            Profiler.reset();
        }

    }


    @Subscribe
    public void handle(AlipayCallbackEvent alipayCallbackEvent) {

        logger.info("the async event has been delayed {}ms", (System.currentTimeMillis() - alipayCallbackEvent.getEventCreated().getTime()));

        checkNotNull(alipayCallbackEvent);
        checkNotNull(alipayCallbackEvent.getAlipayBackDO());


        AlipayBackDOExample alipayBackDOExample = new AlipayBackDOExample();
        alipayBackDOExample.createCriteria().andOutTradeNoEqualTo(alipayCallbackEvent.getAlipayBackDO().getOutTradeNo());

        List<AlipayBackDO> alipayBackDOs = alipayBackDOMapper.selectByExample(alipayBackDOExample);

        int count = 0;

        for (AlipayBackDO alipayBackDO : alipayBackDOs) {
            if (StringUtils.equalsIgnoreCase(alipayBackDO.getTradeStatus(), TRADE_SUCCESS) || StringUtils.equalsIgnoreCase(alipayBackDO.getTradeStatus(), TRADE_FINISH)) {
                count++;
            }
        }

        if (count != 1) {
            logger.warn("the count of the alipay back do not equal 1, ignore ... the trade id is {}", alipayCallbackEvent.getAlipayBackDO().getOutTradeNo());
            return;
        }


        logger.info("#### analytics tracker ####");
        Profiler.start("analytics tracker");

        try {
            AlipayBackDO alipayBackDO = alipayCallbackEvent.getAlipayBackDO();

            Trade trade = queryRelativeTrade(alipayBackDO.getOutTradeNo());

            List<Order> orders = queryOrdersSyncTradeStatus(trade);

            String mobile = queryBuyerMobile(trade);

            logger.info("#### keen io ####");
            Profiler.enter("keen io");

            //keenIOTracker.track(mobile, "pay-callback", transform(alipayCallbackEvent, trade), null);
            //keenIOTracker.track(mobile, "trade", trade, null);
            for (Order order : orders) {
                Map<String, Object> features = ImmutableMap.<String, Object>builder() //
                        .put("tradeId", trade.getId()) //
                        .put("payType", trade.getPayType()) //
                        .build();
                //keenIOTracker.track(mobile, "order", order, features);
            }

            logger.info("#### end ####");
            Profiler.release();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Profiler.release();

            long duration = Profiler.getDuration();

            logger.info("analytics post in {}ms\n{}\n",
                    duration, Profiler.dump("Detail: ", "        "));

            Profiler.reset();
        }
    }

    public PayTrackObject transform(AlipayCallbackEvent alipayCallbackEvent, Trade trade) {
        AlipayBackDO alipayBackDO = alipayCallbackEvent.getAlipayBackDO();
        PayTrackObject payTrackObject = new PayTrackObject();
        payTrackObject.setGmtCreate(trade.getDateCreated());
        payTrackObject.setGmtPayment(alipayBackDO.getGmtPayment());
        payTrackObject.setNotifyTime(alipayCallbackEvent.getEventCreated());
        payTrackObject.setTotalFee((new BigDecimal(alipayBackDO.getTotalFee()).multiply(new BigDecimal(100))).intValue());
        payTrackObject.setBuyerAccount(alipayBackDO.getBuyerEmail());
        payTrackObject.setSellerAccount(alipayBackDO.getSellerEmail());
        payTrackObject.setOutTradeNo(Long.parseLong(alipayBackDO.getOutTradeNo()));
        payTrackObject.setType("alipay");
        return payTrackObject;
    }

    public PayTrackObject transform(WechatCallbackEvent wechatCallbackEvent, Trade trade) {
        WeiXinBackDO weiXinBackDO = wechatCallbackEvent.getWeiXinBackDO();
        PayTrackObject payTrackObject = new PayTrackObject();
        payTrackObject.setGmtCreate(trade.getDateCreated());
        payTrackObject.setGmtPayment(formatter.parseDateTime(weiXinBackDO.getTimeEnd()).toDate());
        payTrackObject.setNotifyTime(wechatCallbackEvent.getEventCreated());
        payTrackObject.setTotalFee(weiXinBackDO.getTotalFee());
        payTrackObject.setBuyerAccount(weiXinBackDO.getOpenid());
        payTrackObject.setSellerAccount(weiXinBackDO.getMchId());
        payTrackObject.setOutTradeNo(Long.parseLong(weiXinBackDO.getOutTradeNo()));
        payTrackObject.setType("wechat");
        return payTrackObject;
    }


    public Trade queryRelativeTrade(String tradeId) {

        checkArgument(isNotBlank(tradeId));

        TradeExample tradeExample = new TradeExample();
        tradeExample.createCriteria().andTradeIdEqualTo(Long.parseLong(tradeId));
        List<Trade> trades = tradeMapper.selectByExample(tradeExample);

        checkArgument(trades.size() == 1, "it can not happen, the size of the trades is not 1 ... the trade_id is [%s]", tradeId);

        return trades.get(0);
    }

    public List<Order> queryOrdersSyncTradeStatus(Trade trade) {

        checkArgument(isNotBlank(trade.getOrders()), "the trade has no order, WTF, the trade id is [%s]", trade.getId());

        List<Long> orderIds = Lists.newArrayList();

        for (String orderStringId : StringUtils.split(trade.getOrders(), ';')) {
            orderIds.add(Long.parseLong(orderStringId));
        }

        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria() //
                .andIdIn(orderIds);

        List<Order> orders = orderMapper.selectByExample(orderExample);

        for (Order order : orders) {
            order.setStatus(trade.getStatus());
        }

        return orders;
    }

    public String queryBuyerMobile(Trade trade) {
        checkNotNull(trade);
        checkNotNull(trade.getBuyerId(), "it can not happen, the buyer id is null, the trade id is [%s]", trade.getId());

        ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(trade.getBuyerId());

        checkNotNull(profileDO);

        Preconditions.checkArgument(isNotBlank(profileDO.getMobile()), "the profile id [{}] with no mobile, WTF", profileDO.getId());

        return profileDO.getMobile();
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
