package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;
import com.mysql.jdbc.Connection;
import com.welink.biz.common.MSG.SMSUtils;
import com.welink.biz.common.constants.TimeConstants;
import com.welink.biz.common.pay.AlipayNotify;
import com.welink.biz.profit.AddProfitImpl;
import com.welink.biz.service.GivePresentService;
import com.welink.biz.service.MikuOneBuyService;
import com.welink.biz.service.MikuUserAgencyService;
import com.welink.biz.service.UsePromotionService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.BuyItemResultCode;
import com.welink.buy.utils.Constants;
import com.welink.buy.utils.TimeUtil;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.commons.TradeUtil;
import com.welink.commons.domain.*;
import com.welink.commons.events.ProfitEvent;
import com.welink.commons.persistence.*;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.ons.TradeMessageProcessFacade;
import com.welink.web.ons.events.TradeEventType;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;

/**
 * Created by daniel on 14-10-27.
 */
@RestController
public class AlipayCallBack {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(AlipayCallBack.class);

    private static final String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    private static final String TRADE_FINISH = "TRADE_FINISHED";
    
    public static final String ALIPAYCALLBACK_FLAG = "alipayCallBack_flag";

    @Resource
    private AlipayBackDOMapper alipayBackDOMapper;

    @Resource
    private PlatformTransactionManager transactionManager;
    
    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private GrouponDOMapper grouponDOMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private AlipayBackSpareDOMapper alipayBackSpareDOMapper;

    @Resource
    private UsePromotionService usePromotionService;
    
    @Resource
    private MikuOneBuyService mikuOneBuyService;

    @Resource
    private TradeMessageProcessFacade tradeMessageProcessFacade;
    
    @Resource
    private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;
    
    @Resource
    private MikuUserAgencyService mikuUserAgencyService;
    
    @Resource
    private AsyncEventBus asyncEventBus;
    
    @Resource
    private GivePresentService givePresentService;
    
    @Resource
    private MikuScratchCardDOMapper mikuScratchCardDOMapper;
    
    @Resource
    private AddProfitImpl addProfitImpl;
    
    
    @RequestMapping(value = {"/api/m/1.0/alipayCallBack.json", "/api/m/1.0/alipayCallBack.htm", "/api/h/1.0/alipayCallBack.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map paramMap = request.getParameterMap();
        Map<String, String> map = new HashMap<>();
        String result = "failed";
        final AlipayBackDO callBackDO = new AlipayBackDO();
        for (Object s : paramMap.keySet()) {
            String v = ((String[]) paramMap.get((String) s))[0];
            map.put((String) s, v);
        }
        buildCallBackDO(map, callBackDO);
        
        /*Object oFlag = memcachedClient.get(ALIPAYCALLBACK_FLAG+callBackDO.getOutTradeNo());
		if (null == oFlag || (null != oFlag && StringUtils.equals("1", oFlag.toString()))) {
            memcachedClient.set(ALIPAYCALLBACK_FLAG+callBackDO.getOutTradeNo(), 2 * 60, "0");
        } else if (null != oFlag && StringUtils.equals("0", oFlag.toString())) {
            return JSON.toJSONString("failed");
        }*/
        
        String mobile = "";
        try {
            TradeExample tradeExample = new TradeExample();
            tradeExample.createCriteria().andTradeIdEqualTo(Long.parseLong(callBackDO.getOutTradeNo()));
            List<Trade> trades = tradeMapper.selectByExample(tradeExample);
            Preconditions.checkArgument(trades.size() == 1);

            ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(trades.get(0).getBuyerId());
            if (null != profileDO) {
                mobile = profileDO.getMobile();
            }

            Preconditions.checkArgument(StringUtils.isNotBlank(mobile), "the profile id [{}] with no mobile, WTF", profileDO.getId());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("fetch profile error. ");
            return JSON.toJSONString("failed");

        }
        EventTracker.track(callBackDO.getBuyerId(), "trade", "callback-pre", callBackDO.getTradeStatus(), 1L);
        EventTracker.track(callBackDO.getOutTradeNo(), "trade", "callback-pre", callBackDO.getTradeStatus(), 1L);
        EventTracker.track(callBackDO.getBuyerId(), "trade", "callback-pre", callBackDO.getOutTradeNo(), 1L);
        EventTracker.track(mobile, "trade", "callback-pre", callBackDO.getOutTradeNo(), 1L);
        log.info("alipay callback out trade no.:" + callBackDO.getOutTradeNo() + ",status:" + callBackDO.getTradeStatus());
        if (StringUtils.equalsIgnoreCase(callBackDO.getTradeStatus(), "TRADE_FINISHED")) {
            result = "success";
            log.info("trade finished return......alipay callback out trade no.:" + callBackDO.getOutTradeNo() + ",status:" + callBackDO.getTradeStatus());
            return JSON.toJSONString(result);
        }
        
        //存储call back 信息 存储失败
        if (storeAlipayBackInfo(callBackDO)) {
            return JSON.toJSONString("failed");
        }

        boolean verify = AlipayNotify.verify(map);
        if (verify) {
            com.welink.web.common.filter.Profiler.enter("alipay callback transaction");
            long tradeId = Long.valueOf(callBackDO.getOutTradeNo());
            TradeExample tExample = new TradeExample();
            tExample.createCriteria().andTradeIdEqualTo(tradeId);
            List<Trade> outTrades = tradeMapper.selectByExample(tExample);
            Trade outTrade = null;
            if (null != outTrades && outTrades.size() > 0) {
                outTrade = outTrades.get(0);
            } else {
                String msg = "outTradeNO:" + callBackDO.getOutTradeNo() + " buyerId:" + callBackDO.getBuyerId();
                //SMSUtils.sendSmsForAlipayBack(msg, "18605816526");
            }

            if (null != outTrade) {
                TradeInfo tradeInfo = new TradeInfo();
                tradeInfo.setTrade(outTrade);
                tradeInfo.setAlipayBackDO(callBackDO);
                tradeInfo.setChangeStatus(false);
                boolean appointDeal = appointServiceDeal(tradeInfo, outTrade);
                if (appointDeal) {
                	if (null != outTrade.getTradeId() && outTrade.getTradeId() > 0) {	//交易分润
                		asyncEventBus.post(new ProfitEvent(outTrade.getTradeId(), 1));	//通过事件总线 交易分润 //1=加分润；2=减分润
                	}
                    usePromotionService.changePromotionFrozenToUsed(outTrade.getTradeId(), outTrade.getBuyerId());
                    //mikuOneBuyService.calculateOneBuyRewardByItemId(itemId);
                    //赠品操作
                    givePresentService.giveOnePresentToConsumerStander(outTrade.getTradeId(), outTrade.getBuyerId());
                    result = "success";
                } else {
                    result = "failed";
                }
            } else {
                log.info("insert alipay call back msg failed. cant find trade. out_trade_no:" + callBackDO.getOutTradeNo() + ",tradeNo:" + callBackDO.getTrade_no());
                result = "failed";
                return JSON.toJSONString(result);
            }
        } else {
            log.info("insert alipay call back msg failed. verify failed. out_trade_no:" + callBackDO.getOutTradeNo() + ",tradeNo:" + callBackDO.getTrade_no());
            result = "failed";
            return JSON.toJSONString(result);
        }

        EventTracker.track(callBackDO.getBuyerId(), "trade", "callback-success", callBackDO.getTradeStatus(), 1L);
        EventTracker.track(callBackDO.getOutTradeNo(), "trade", "callback-success", callBackDO.getTradeStatus(), 1L);
        EventTracker.track(callBackDO.getBuyerId(), "trade", "callback-success", callBackDO.getOutTradeNo(), 1L);
        EventTracker.track(mobile, "trade", "callback-success", callBackDO.getOutTradeNo(), 1L);
        result = "success";
        //return JSON.toJSONString(result);
        return result;
    }
    
    private void buildCallBackDO(Map<String, String> map, AlipayBackDO callBackDO) {
        for (String s : map.keySet()) {
            String v = map.get(s);
            if (StringUtils.equals("discount", s)) {
                callBackDO.setDiscount(v);
            }
            if (StringUtils.equals("payment_type", s)) {
                callBackDO.setPaymentType(v);
            }
            if (StringUtils.equals("subject", s)) {
                callBackDO.setSubject(v);
            }
            if (StringUtils.equals("trade_no", s)) {
                callBackDO.setTrade_no(v);
            }
            if (StringUtils.equals("buyer_email", s)) {
                callBackDO.setBuyerEmail(v);
            }
            if (StringUtils.equals("notify_type", s)) {
                callBackDO.setNotifyType(v);
            }
            if (StringUtils.equals("quantity", s)) {
                callBackDO.setQuantity(v);
            }
            if (StringUtils.equals("out_trade_no", s)) {
                callBackDO.setOutTradeNo(v);
            }
            if (StringUtils.equals("seller_id", s)) {
                callBackDO.setSellerId(v);
            }
            if (StringUtils.equals("notify_time", s)) {
                try {
                    callBackDO.setNotifyTime(TimeUtil.str2DateTime(v));
                } catch (Exception e) {
                    log.error("alipay call back parse notify_time error. notify_time:" + v);
                }
            }
            if (StringUtils.equals("gmt_payment", s)) {
                try {
                    callBackDO.setGmtPayment(TimeUtil.str2DateTime(v));
                } catch (Exception e) {
                    log.error("alipay call back parse gmt_payment error. gmt_payment:" + v);
                }
            }
            if (StringUtils.equals("gmt_create", (String) s)) {
                try {
                    callBackDO.setGmtCreate(TimeUtil.str2DateTime(v));
                } catch (Exception e) {
                    log.error("alipay call back parse gmt_create error. notify_time:" + v);
                }
            }
            if (StringUtils.equals("body", s)) {
                callBackDO.setBody(v);
            }
            if (StringUtils.equals("trade_status", s)) {
                callBackDO.setTradeStatus(v);
            }
            if (StringUtils.equals("is_total_fee_adjust", s)) {
                callBackDO.setIsTotalFeeAdjust(v);
            }
            if (StringUtils.equals("total_fee", s)) {
                callBackDO.setTotalFee(v);
            }
            if (StringUtils.equals("seller_email", s)) {
                callBackDO.setSellerEmail(v);
            }
            if (StringUtils.equals("price", s)) {
                callBackDO.setPrice(v);
            }
            if (StringUtils.equals("buyer_id", s)) {
                callBackDO.setBuyerId(v);
            }
            if (StringUtils.equals("notify_id", s)) {
                callBackDO.setNotifyId(v);
            }
            if (StringUtils.equals("use_coupon", s)) {
                callBackDO.setUseCoupon(v);
            }
            if (StringUtils.equals("sign_type", s)) {
                callBackDO.setSignType(v);
            }
            if (StringUtils.equals("sign", s)) {
                callBackDO.setSign(v);
            }
        }
        callBackDO.setDateCreated(new Date());
    }
    
    private boolean appointServiceDeal(final TradeInfo tradeInfo, final Trade outTrade) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("change-trade-order-status");
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        transactionTemplate.execute(new TransactionCallback<TradeInfo>() {

            @Override
            public TradeInfo doInTransaction(TransactionStatus transactionStatus) {
                //获取参数
                if (StringUtils.equals(tradeInfo.getAlipayBackDO().getTradeStatus(), TRADE_SUCCESS)) {
                	Trade trade2 = tradeInfo.getTrade();
                	if(null != trade2 && null != trade2.getBuyerId() 
                			&& trade2.getType().equals(Constants.TradeType.join_agency.getTradeTypeId())){
                		//当为成为代理订单时更新购买者为代理
                		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(trade2.getBuyerId());
                		if(null != profileDO && profileDO.getIsAgency().equals((byte)0)){
                			//当购买者不是代理时
                			profileDO.setIsAgency((byte)1);	//1=代理
                			profileDO.setLastUpdated(new Date());
                			if(profileDOMapper.updateByPrimaryKey(profileDO) < 1){
                				transactionStatus.setRollbackOnly();
                				tradeInfo.setChangeStatus(false);
                				return tradeInfo;
                			}
                			//建立代理关系
        		        	int insertMikuUserAgency = mikuUserAgencyService.updateMikuUserAgency(trade2.getpUserId(), trade2.getBuyerId(), transactionStatus);	
        		        	if(insertMikuUserAgency < 1){
        		        		transactionStatus.setRollbackOnly();
                				tradeInfo.setChangeStatus(false);
                				return tradeInfo;
        		        	}
                		}
                		
        			}
                	
                	if(null != trade2 && null != trade2.getBuyerId() && null != trade2.getType()
                			&& trade2.getType().equals(Constants.TradeType.scratch_card.getTradeTypeId())){
                		//如果是刮刮卡订单
                		MikuScratchCardDO mikuScratchCardDO = null;
                		MikuScratchCardDOExample mikuScratchCardDOExample = new MikuScratchCardDOExample();
                		mikuScratchCardDOExample.createCriteria().andUserIdEqualTo(trade2.getBuyerId())
                			.andTradeIdEqualTo(trade2.getTradeId());
                		List<MikuScratchCardDO> mikuScratchCardDOList = mikuScratchCardDOMapper.selectByExample(mikuScratchCardDOExample);
                    	if(!mikuScratchCardDOList.isEmpty()){
                    		mikuScratchCardDO = mikuScratchCardDOList.get(0);
                    	}
                		if(null != mikuScratchCardDO){
                    		mikuScratchCardDO.setStatus(Constants.ScratchCardStatus.PAYED.getStatusId());
                    		mikuScratchCardDO.setLastUpdated(new Date());
                    		if(mikuScratchCardDOMapper.updateByPrimaryKeySelective(mikuScratchCardDO) < 1){	//更新刮刮卡状态为未付款
                    			transactionStatus.setRollbackOnly();
                    			tradeInfo.setChangeStatus(false);
                                return tradeInfo;
                    		}
                    	}
                	}
                	
                    //设置订单状态
                    Trade trade = new Trade();
                    trade.setPayment(TradeUtil.getLongPrice(tradeInfo.getAlipayBackDO().getTotalFee()));
                    trade.setPayType(Constants.PayType.ONLINE_ALIPAY.getPayTypeId());
                    trade.setAlipayNo(tradeInfo.getAlipayBackDO().getTrade_no());
                    trade.setLastUpdated(new Date());
                    trade.setPayTime(new Date());
                    if(null != tradeInfo.getTrade() && null != tradeInfo.getTrade().getType() 
                    		&& tradeInfo.getTrade().getType() == Constants.TradeType.one_buy_type.getTradeTypeId()){
                    	trade.setStatus(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
                    }else{
                    	trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                    }
                    trade.setCodStatus(Constants.CodStatus.SIGN_IN.getCodStatusId());
                    trade.setVersion(tradeInfo.getTrade().getVersion() + 1l);
                    TradeExample tExample = new TradeExample();
                    List<Byte> toUpdateStatus = new ArrayList<>();
                    toUpdateStatus.add((byte) 2);
                    toUpdateStatus.add((byte) 9);
                    tExample.createCriteria().andTradeIdEqualTo(Long.valueOf(tradeInfo.getAlipayBackDO().getOutTradeNo())).andVersionEqualTo(tradeInfo.getTrade().getVersion())
                            .andStatusIn(toUpdateStatus);
                    if (tradeMapper.updateByExampleSelective(trade, tExample) < 1) {
                        log.error("alipay pay . update trade status failed. trade id:" + trade.getTradeId());
                        transactionStatus.setRollbackOnly();
                        tradeInfo.setChangeStatus(false);
                        return tradeInfo;
                    } else {
                        tradeMessageProcessFacade.sendMessage(tradeInfo.getTrade().getTradeId(), TradeEventType.TRADE_BUYER_PAY.getTopic());
                    }
                    //设置order状态
                    List<Order> orders = new ArrayList<Order>();
                    if (tradeInfo.getTrade().getOrders().length() > 0) {
                        for (String id : tradeInfo.getTrade().getOrders().split(";")) {
                            OrderExample orderExample = new OrderExample();
                            orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
                            List<Order> tempOrders = orderMapper.selectByExample(orderExample);
                            if (null != orders && tempOrders.size() > 0) {
                                orders.add(tempOrders.get(0));
                            }
                        }
                        for (Order order : orders) {
                            Order tmpOrder = new Order();
                            tmpOrder.setLastUpdated(new Date());
                            tmpOrder.setPayment(order.getTotalFee());
                            if (StringUtils.equals(tradeInfo.getAlipayBackDO().getTradeStatus(), TRADE_SUCCESS) && StringUtils.equals(tradeInfo.getAlipayBackDO().getTradeStatus(), TRADE_SUCCESS)) {
                                //tmpOrder.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                                if(null != tradeInfo.getTrade() && null != tradeInfo.getTrade().getType() 
                                		&& tradeInfo.getTrade().getType() == Constants.TradeType.one_buy_type.getTradeTypeId()){
                                	tmpOrder.setStatus(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
                                }else{
                                	tmpOrder.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                                }
                                tmpOrder.setVersion(order.getVersion() + 1l);
                                OrderExample oExample = new OrderExample();
                                oExample.createCriteria().andIdEqualTo(order.getId()).andStatusEqualTo((byte) 2);
                                if(orderMapper.updateByExampleSelective(tmpOrder, oExample) < 1){
                                	transactionStatus.setRollbackOnly();
                                	tradeInfo.setChangeStatus(false);
                                	return tradeInfo;
                                }
                                //更新库存和销售量 针对非物流订单
                                //updateStock(order, transactionStatus);

                            } else {
                                tmpOrder.setVersion(order.getVersion() + 1l);
                                OrderExample oExample = new OrderExample();
                                oExample.createCriteria().andIdEqualTo(order.getId()).andStatusEqualTo((byte) 4);
                                if(orderMapper.updateByExampleSelective(tmpOrder, oExample) < 1){
                                	transactionStatus.setRollbackOnly();
                                	tradeInfo.setChangeStatus(false);
                                	return tradeInfo;
                                }
                            }
                        }
                    }
                    //更新一元购订单为已付款
                    /*if(!mikuOneBuyService.updateUserOneBuyByTrade(tradeInfo.getTrade().getTradeId(), (byte)1)){	//status(-1=取消;0=未付款;1=已付款)
                    	transactionStatus.setRollbackOnly();
                    	tradeInfo.setChangeStatus(false);
                    	return tradeInfo;
                    }*/
                    tradeInfo.setChangeStatus(true);
                } else if (StringUtils.equals(tradeInfo.getAlipayBackDO().getTradeStatus(), TRADE_FINISH)) {
                    //无法进行进一步支付宝操作
                    log.info("======trade finished. tradeId:" + tradeInfo.getTrade().getTradeId());
                } else {
                    log.info("======trade call back unkoun status. tradeId:" + tradeInfo.getAlipayBackDO().getOutTradeNo() + ",status:" +
                            tradeInfo.getAlipayBackDO().getTradeStatus());
                }
                return tradeInfo;
            }
        });
        return tradeInfo.isChangeStatus();
    }

    /**
     * 存储支付宝回调信息
     *
     * @param callBackDO
     * @return
     */
    private boolean storeAlipayBackInfo(AlipayBackDO callBackDO) {
        //存储更新
        if (alipayBackDOMapper.insertSelective(callBackDO) < 0) {
            log.error("insert alipay call back msg failed. out_trade_no:" + callBackDO.getOutTradeNo() + ",tradeNo:" + callBackDO.getTrade_no());
            return true;
        }
        return false;
    }

    /**
     * 存储因为事务异常出现的回调信息
     *
     * @param callBackDO
     * @return
     */
    private boolean storeAlipayBackSpareInfo(AlipayBackDO callBackDO) {
        AlipayBackSpareDO spareDO = new AlipayBackSpareDO();
        spareDO.setBody(callBackDO.getBody());
        spareDO.setBuyerEmail(callBackDO.getBuyerEmail());
        spareDO.setBuyerId(callBackDO.getBuyerId());
        spareDO.setDateCreate(callBackDO.getDateCreated());
        spareDO.setDiscount(callBackDO.getDiscount());
        spareDO.setGmtCreate(callBackDO.getGmtCreate());
        spareDO.setGmtPayment(callBackDO.getGmtPayment());
        spareDO.setIsTotalFeeAdjust(callBackDO.getIsTotalFeeAdjust());
        spareDO.setNotifyId(callBackDO.getNotifyId());
        spareDO.setNotifyTime(callBackDO.getNotifyTime());
        spareDO.setNotifyType(callBackDO.getNotifyType());
        spareDO.setOutTradeNo(callBackDO.getOutTradeNo());
        spareDO.setPaymentType(callBackDO.getPaymentType());
        spareDO.setPrice(callBackDO.getPrice());
        spareDO.setQuantity(callBackDO.getQuantity());
        spareDO.setSellerEmail(callBackDO.getSellerEmail());
        spareDO.setSellerId(callBackDO.getSellerId());
        spareDO.setSign(callBackDO.getSign());
        spareDO.setSignType(callBackDO.getSignType());
        spareDO.setSubject(callBackDO.getSubject());
        spareDO.setTotalFee(callBackDO.getTotalFee());
        spareDO.setTrade_no(callBackDO.getTrade_no());
        spareDO.setTradeStatus(callBackDO.getTradeStatus());
        spareDO.setUseCoupon(callBackDO.getUseCoupon());
        spareDO.setDateCreate(new Date());
        spareDO.setVersion(0l);
        spareDO.setStatus((byte) 0);
        if (alipayBackSpareDOMapper.insertSelective(spareDO) < 0) {
            log.error("insert alipay call back spare msg failed. out_trade_no:" + callBackDO.getOutTradeNo() + ",tradeNo:" + callBackDO.getTrade_no());
            return false;
        }
        return true;
    }

    /**
     * 更新库存
     *
     * @param order
     * @return
     */
    private boolean updateStock(Order order, TransactionStatus transactionStatus) {
        if (Long.compare(order.getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
            long itemId = order.getArtificialId();
            long num = order.getNum();
            GrouponDOExample gExample = new GrouponDOExample();
            gExample.createCriteria().andItemIdEqualTo(itemId).andStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus());
            gExample.setOrderByClause("online_end_time DESC");
            List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
            //是团购商品
            if (null != grouponDOs && grouponDOs.size() > 0) {
                long currentStock = grouponDOs.get(0).getQuantity();
                long soldQuantity = grouponDOs.get(0).getSoldQuantity();
                long grouponId = grouponDOs.get(0).getId();
                long version = grouponDOs.get(0).getVersion();
                currentStock = currentStock - num;
                if (currentStock < 0) {
                    currentStock = 0;
                }
                soldQuantity = soldQuantity + num;
                GrouponDO grouponDO = new GrouponDO();
                grouponDO.setQuantity(currentStock);
                grouponDO.setSoldQuantity(soldQuantity);
                grouponDO.setVersion(version + 1l);
                GrouponDOExample gExample1 = new GrouponDOExample();
                gExample1.createCriteria().andIdEqualTo(grouponId).andVersionEqualTo(version);
                if (grouponDOMapper.updateByExampleSelective(grouponDO, gExample1) < 1) {
                    log.error("update groupon stock quantity failed. itemId:" + itemId + ",num:" + num);
                    return true;
                } else {
                    //更新商品销售数量
                    Item item = itemMapper.selectByPrimaryKey(itemId);
                    if (null != item) {
                        long itemSoldQuantity = 0;
                        if (null == item.getSoldQuantity()) {
                            itemSoldQuantity = 0;
                        } else {
                            if (null == item.getSoldQuantity()) {
                                itemSoldQuantity = 0;
                            } else {
                                itemSoldQuantity = item.getSoldQuantity();
                            }
                        }
                        itemSoldQuantity = itemSoldQuantity + num;
                        Item item1 = new Item();
                        item1.setSoldQuantity((int) itemSoldQuantity);
                        long itemNum = item.getNum();
                        itemNum = itemNum - num;
                        item1.setNum((int) itemNum);
                        item1.setVersion(item.getVersion() + 1l);
                        item1.setLastUpdated(new Date());
                        ItemExample iExample = new ItemExample();
                        iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                        if (itemMapper.updateByExampleSelective(item1, iExample) < 1) {
                            log.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                            com.welink.web.common.filter.Profiler.release();
                            return true;
                        }
                    }
                }
            } else {//非团购商品  更新item表中的库存
                Item item = itemMapper.selectByPrimaryKey(itemId);
                if (null != item) {
                    long itemSoldQuantity = 0;
                    if (null == item.getSoldQuantity()) {
                        itemSoldQuantity = 0;
                    } else {
                        if (null == item.getSoldQuantity()) {
                            itemSoldQuantity = 0;
                        } else {
                            itemSoldQuantity = item.getSoldQuantity();
                        }
                    }
                    itemSoldQuantity = itemSoldQuantity + num;
                    Item item1 = new Item();
                    item1.setSoldQuantity((int) itemSoldQuantity);
                    long itemNum = item.getNum();
                    itemNum = itemNum - num;
                    ItemExample iExample = new ItemExample();
                    item1.setVersion(item.getVersion() + 1l);
                    item1.setLastUpdated(new Date());
                    if (itemNum <= 0) {
                        //下架商品
                        item1.setNum(0);
                        item1.setApproveStatus(BizConstants.ItemApproveStatus.OFF_SALE.getStatus());
                        iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                    } else {
                        item1.setNum((int) itemNum);
                        iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                    }
                    if (itemMapper.updateByExampleSelective(item1, iExample) < 1) {
                        log.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                        com.welink.web.common.filter.Profiler.release();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    class TradeInfo {
        Trade trade;
        AlipayBackDO alipayBackDO;
        boolean changeStatus;

        public Trade getTrade() {
            return trade;
        }

        public void setTrade(Trade trade) {
            this.trade = trade;
        }

        public AlipayBackDO getAlipayBackDO() {
            return alipayBackDO;
        }

        public void setAlipayBackDO(AlipayBackDO alipayBackDO) {
            this.alipayBackDO = alipayBackDO;
        }

        public boolean isChangeStatus() {
            return changeStatus;
        }

        public void setChangeStatus(boolean changeStatus) {
            this.changeStatus = changeStatus;
        }
    }
}
