package com.welink.biz.resources;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.Connection;
import com.welink.biz.common.constants.TimeConstants;
import com.welink.biz.service.BannerService;
import com.welink.biz.service.EvalService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ProfitService;
import com.welink.biz.service.PushService;
import com.welink.biz.service.TradeService;
import com.welink.biz.service.UsePromotionService;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.AlipayBackSpareDO;
import com.welink.commons.domain.AlipayBackSpareDOExample;
import com.welink.commons.domain.GrouponDO;
import com.welink.commons.domain.GrouponDOExample;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.ObjectTaggedDO;
import com.welink.commons.domain.ObjectTaggedDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.TagsDO;
import com.welink.commons.domain.TagsDOExample;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeCourierDO;
import com.welink.commons.domain.TradeCourierDOExample;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.AlipayBackSpareDOMapper;
import com.welink.commons.persistence.GrouponDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.ObjectTaggedDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.TagsDOMapper;
import com.welink.commons.persistence.TradeCourierDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.tools.Profiler;

/**
 * Created by daniel on 14-11-14.
 */
public class TradeCheckerTask extends QuartzJobBean {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TradeCheckerTask.class);

    public static final String TRADE_TASK_FLAG = "trade_task_flag";

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private PushService pushService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private EvalService evalService;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private GrouponDOMapper grouponDOMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private AlipayBackSpareDOMapper alipayBackSpareDOMapper;

    @Resource
    private UsePromotionService usePromotionService;

    @Resource
    private BannerService bannerService;

    @Resource
    private MemcachedClient memcachedClient;
    
    @Resource
    private ItemService itemService;
    
    @Resource
    private TradeService tradeService;
    
    @Resource
    private TradeCourierDOMapper tradeCourierDOMapper;
    
    @Resource
    private TagsDOMapper tagsDOMapper;
    
    @Resource
    private ObjectTaggedDOMapper objectTaggedDOMapper;
    
    /*@Resource
    private AsyncEventBus asyncEventBus;*/
    
    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    private static final String TRADE_FINISH = "TRADE_FINISHED";
    
    @Resource
    private ProfitService profitService;

    /*private AsyncEventBus asyncEventBus;

    public void setAsyncEventBus(AsyncEventBus asyncEventBus) {
        this.asyncEventBus = asyncEventBus;
    }*/
    
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Random random = new Random();
        long sec = random.nextInt(10) * 1000l;
        try {
            Thread.sleep(sec);
        } catch (InterruptedException e) {
            logger.error("trade checker task . thread sleep failed. exp:" + e.getMessage());
        }
        Object oFlag = memcachedClient.get("trade_task_flag");
        if (null == oFlag || (null != oFlag && StringUtils.equals("1", oFlag.toString()))) {
            memcachedClient.set(TRADE_TASK_FLAG, TimeConstants.REDIS_EXPIRE_SECONDS_5, "0");
        } else if (null != oFlag && StringUtils.equals("0", oFlag.toString())) {
            return;
        }
        
        List<Byte> toDealStatus = new ArrayList<>();	
        toDealStatus.add(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
        toDealStatus.add(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
        toDealStatus.add(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
        //toDealStatus.add(Constants.TradeStatus.TRADE_COMMENTED.getTradeStatusId());
        //toDealStatus.add(Constants.TradeStatus.TRADE_RETURNNING.getTradeStatusId());
        
        List<Byte> toSuccessedStatus = new ArrayList<>();	//更新为交易完成之前的交易状态
    	toSuccessedStatus.add(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
    	//toSuccessedStatus.add(Constants.TradeStatus.TRADE_COMMENTED.getTradeStatusId());
    	//toSuccessedStatus.add(Constants.TradeStatus.TRADE_RETURNNING.getTradeStatusId());
        
        Date nowDate = new Date();
        
        //统一处理
        TradeExample tExample = new TradeExample();
        tExample.createCriteria().andTimeoutActionTimeGreaterThan(addDay(nowDate, -7)).andStatusIn(toDealStatus);//.andTimeoutActionTimeBetween(addDay(new Date(), -7), addDay(new Date(), 7));
        List<Trade> trades = tradeMapper.selectByExample(tExample);
        List<Long> toCloseTradeIds = new ArrayList<>();
        List<Long> toConfirmTradeIds = new ArrayList<>();
        List<Long> toEvalTradeIds = new ArrayList<>();	//更新为已评价的交易ids
        List<Long> toFinishIds = new ArrayList<>();		//更新为确认收货的交易ids
        List<Long> toSuccessedIds = new ArrayList<>();	//更新为交易完成的交易ids
        long buyerId = -1;
        if (null != trades && trades.size() > 0) {
            Date dn = new Date();
            for (Trade t : trades) {
                buyerId = t.getBuyerId();
                //1. 一天内未付款的订单取消掉
                //if (t.getStatus() == Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId() && t.getTimeoutActionTime().getTime() <= dn.getTime()) {
                if (t.getStatus() == Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId() && 
                		(new DateTime(t.getDateCreated().getTime()).plusMinutes(BizConstants.TRADE_OUT_OF_DATE_MINUTE_24_HOUR).toDate()).getTime() <= dn.getTime()) {
                	toCloseTradeIds.add(t.getTradeId());
                    //1.1 push message
                    pushService.pushMsg(BizConstants.PushActionEnum.ALERT, "报告当家的：您的订单" + t.getTradeId() + "由于超时未支付，已自动取消，如您还需购买请重新下单，给您带来不便，敬请谅解，小的时刻准备为您服务！", null, t.getBuyerId(), BizConstants.PushRedirectEnum.TRADE_DETAIL.getAction(), t.getTradeId());
                }//2. 已经派送的订单3天后完成
//                else if (t.getStatus() == Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId() && t.getTimeoutActionTime().getTime() <= dn.getTime()) {
//                    toConfirmTradeIds.add(t.getTradeId());
//                    pushService.pushMsg(BizConstants.PushActionEnum.ALERT, "报告当家的：您可以对订单" + toConfirmTradeIds.get(0) + "进行评价了哦", null, buyerId, BizConstants.PushRedirectEnum.EVAL.getAction(), t.getTradeId());
//                }//3. 7天如果未评价默认好评和状态改为已完成(TRADE_SUCCESSED)
                /*else if (t.getStatus() == Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId() && t.getTimeoutActionTime().getTime() <= dn.getTime() && t.getBuyerRate() != Constants.RateType.RATED.getRateTypeId()) {
                	//更新为已评价的交易ids
                	toEvalTradeIds.add(t.getTradeId());
                } */
                else if (toSuccessedStatus.contains(t.getStatus())
                		&& t.getTimeoutActionTime().getTime() <= dn.getTime() ) {
                	//更新为交易完成的交易ids
                	toSuccessedIds.add(t.getTradeId());
                } else{
                	//配送后24小时自动完成 
                	if (null != t.getStatus() && t.getStatus() == Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId() 
                			&& !t.getReturnStatus().equals(Constants.TradeReturnStatus.RETURNED.getStatusId()) && t.getTimeoutActionTime().getTime() <= dn.getTime()) {
                		//更新为确认收货的交易ids
                		toFinishIds.add(t.getTradeId());
                	}
                }
            }

            if (toCloseTradeIds.size() > 0) {
            	//取消订单
                //先查询
                TradeExample tqExample = new TradeExample();
                tqExample.createCriteria().andTradeIdIn(toCloseTradeIds).andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                List<Trade> tradeList = tradeMapper.selectByExample(tqExample);
                if (null != tradeList && tradeList.size() > 0) {
                    for (Trade trade : tradeList) {
                    	if(null != trade){
                    		tradeService.cancelOrder(trade);	//取消订单
                    		/*if (null != trade && trade.getStatus() == Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId()) {
    			                trade.setStatus(Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId());
    			                trade.setVersion(trade.getVersion() + 1);
    			                trade.setLastUpdated(new Date());
    			                trade.setEndTime(new Date());
    			                
    			                TradeExample tradeExample = new TradeExample();
    			                tradeExample.createCriteria().andTradeIdEqualTo(trade.getTradeId());
    			                tradeMapper.updateByExampleSelective(trade, tradeExample);
    			                usePromotionService.changePromotionFrozenToUnUsed(trade.getTradeId(), trade.getBuyerId());
    			                //删除购买记录
    			                itemService.deleteBuyRecord(trade);
    			                
    			                List<Order> orderList = findOrdersByTradeId(trade.getTradeId());	//根据交易号查找订单列表
    			                if(null != orderList && !orderList.isEmpty()){
    			                	for(Order order : orderList){
    			                		if(null != order){
    			                			updateStock2(order, null, 2);		//更新库存(2=加库存)
    			                		}
    			                	}
    			                }
    			            } else if (trade.getStatus() == Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId()) {
    			                itemService.deleteBuyRecord(trade);
    			            }*/
                    	}
                    	
                        /*TradeExample tradeExample = new TradeExample();
                        tradeExample.createCriteria().andTradeIdEqualTo(t.getTradeId()).andVersionEqualTo(t.getVersion());
                        Trade trade = new Trade();
                        trade.setVersion(t.getVersion() + 1l);
                        trade.setLastUpdated(new Date());
                        trade.setStatus(Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId());
                        //再更新
                        if (tradeMapper.updateByExampleSelective(trade, tradeExample) < 1) {
                            logger.error("update trade status . 30m cancel trade failed. tradeId:" + t.getTradeId());
                        }
                        
                        if(null != t.getTradeId()){
                        	List<Order> orderList = findOrdersByTradeId(t.getTradeId());	//根据交易号查找订单列表
                        	if(null != orderList && !orderList.isEmpty()){
                        		for(Order order : orderList){
                        			if(null != order){
                        				updateStock2(order, null, 2);		//更新库存(2=加库存)
                        			}
                        		}
                        	}
                        }
                        //关闭未付款订单时将积分和优惠券由冻结状态恢复成未使用
                        usePromotionService.changePromotionFrozenToUnUsed(t.getTradeId(), t.getBuyerId());*/
                    }
                }
            }

            if (toFinishIds.size() > 0) {
                TradeExample tqExample = new TradeExample();
                tqExample.createCriteria().andTradeIdIn(toFinishIds).andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
                List<Trade> tradeList = tradeMapper.selectByExample(tqExample);
                if (null != tradeList && tradeList.size() > 0) {
                    for (Trade t : tradeList) {
                        /*TradeExample uExample = new TradeExample();
                        uExample.createCriteria().andTradeIdEqualTo(t.getTradeId()).andVersionEqualTo(t.getVersion());
                        Trade trade = new Trade();
                        trade.setVersion(t.getVersion() + 1l);
                        trade.setStatus(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
                        trade.setCanRate((byte) 1);
                        trade.setLastUpdated(new Date());
                        trade.setTimeoutActionTime(addDay(dn, 7));
                        if (tradeMapper.updateByExampleSelective(trade, uExample) < 0) {
                            logger.error("update trade status from 5 - 7 failed. tradeId:" + t.getTradeId());
                        }*/
                        if(null != t.getTradeId() && !"".equals(t.getTradeId().toString())){
                        	//asyncEventBus.post(new ProfitEvent(t.getTradeId()));	//通过事件总线 交易分润
                        	profitService.sureTrade(t.getId());			//确认订单
                        	//sureTrade(trade.getId());			//确认订单
                        }
                    }
                }
            }
//            if (toConfirmTradeIds.size() > 0) {
//                //先查询
//                TradeExample tqExample = new TradeExample();
//                tqExample.createCriteria().andTradeIdIn(toConfirmTradeIds).andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
//                List<Trade> tradeList = tradeMapper.selectByExample(tqExample);
//                if (null != tradeList && tradeList.size() > 0) {
//                    for (Trade t : tradeList) {
//                        TradeExample tradeExample = new TradeExample();
//                        tradeExample.createCriteria().andTradeIdEqualTo(t.getTradeId()).andVersionEqualTo(t.getVersion());
//                        Trade trade = new Trade();
//                        trade.setStatus(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
//                        trade.setCanRate((byte) 1);
//                        trade.setVersion(t.getVersion() + 1l);
//                        trade.setTimeoutActionTime(addDay(dn, 7));
//                        if (tradeMapper.updateByExampleSelective(trade, tradeExample) < 0) {
//                            logger.error("update trade status . 3day finished trade failed. tradeId:" + t.getTradeId());
//                        }
//                    }
//                }
//            }
            if (toEvalTradeIds.size() > 0) {
            	/*//把确认收货超过7天的交易更新为交易完成
            	TradeExample tradeExample = new TradeExample();//buyerRate
                tradeExample.createCriteria().andTradeIdIn(toEvalTradeIds).andTimeoutActionTimeGreaterThan(nowDate)
                        .andStatusEqualTo(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
                Trade tradeSuccessed = new Trade();
                tradeSuccessed.setStatus(Constants.TradeStatus.TRADE_SUCCESSED.getTradeStatusId());
                tradeMapper.updateByExampleSelective(tradeSuccessed, tradeExample);*/
            	
            	/*TradeExample tradeExample = new TradeExample();//buyerRate
                tradeExample.createCriteria().andTradeIdIn(toEvalTradeIds).andBuyerRateNotEqualTo(Constants.RateType.RATED.getRateTypeId())
                        .andStatusEqualTo(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
                if (toEvalTradeIds.size() > 0) {
                    List<Trade> tradeList = tradeMapper.selectByExample(tradeExample);
                    if (null != tradeList && tradeList.size() > 0) {
                        for (Trade trade : tradeList) {
                            List<Order> orders = new ArrayList<Order>();
                            List<Order> tmpOrders = new ArrayList<Order>();
                            //fetch orders
                            if (trade.getOrders().length() > 0) {
                                for (String id : trade.getOrders().split(";")) {
                                    OrderExample orderExample = new OrderExample();
                                    orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
                                    orders = orderMapper.selectByExample(orderExample);
                                    if (null != orders && orders.size() > 0) {
                                        tmpOrders.add(orders.get(0));
                                    }
                                }
                            }
                            //eval
                            if (null != orders && orders.size() > 0) {
                                for (Order o : orders) {
                                    if (Long.compare(o.getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
                                        if (!evalService.addEval(BizConstants.WELINK_RATE_ID, trade.getTradeId(), o.getId(), o.getArtificialId(), (byte) 5,
                                                null, null, null, null, null, BizConstants.OrderEvalType.SYSTEM_EVAL.getType())) {
                                            logger.error("add eval failed. tradeId:" + trade.getTradeId() + ",itemId:" + o.getArtificialId());
                                        }
                                    }
                                }
                            }
                            //更新trade
                            Trade trade1 = new Trade();
                            trade1.setBuyerRate(Constants.RateType.RATED.getRateTypeId());
                            trade1.setCanRate((byte) 0);
                            trade1.setVersion(trade.getVersion() + 1l);
                            trade1.setLastUpdated(new Date());
                            TradeExample utradeExample = new TradeExample();
                            tExample.createCriteria().andTradeIdEqualTo(trade.getTradeId()).andVersionEqualTo(trade.getVersion());
                            if (tradeMapper.updateByExampleSelective(trade1, utradeExample) < 1) {
                                logger.error("update trade rate status failed. tradeId:" + trade.getTradeId());
                            }
                        }
                    }
                }*/
            }
            
            if(toSuccessedIds.size() > 0){
            	//把确认收货、已评价、退货中等超过7天过期的订单更新为交易完成
            	TradeExample tradeExample = new TradeExample();//buyerRate
                tradeExample.createCriteria().andTradeIdIn(toSuccessedIds).andTimeoutActionTimeLessThan(nowDate)
                        .andStatusIn(toSuccessedStatus).andReturnStatusNotEqualTo(Constants.TradeReturnStatus.RETURNED.getStatusId());
                Trade tradeSuccessed = new Trade();
                tradeSuccessed.setStatus(Constants.TradeStatus.TRADE_SUCCESSED.getTradeStatusId());
                tradeSuccessed.setReturnStatus(Constants.TradeReturnStatus.NORMAL.getStatusId());
                tradeMapper.updateByExampleSelective(tradeSuccessed, tradeExample);
                
            }
            
        }

        //更新付款状态及后续处理
        AlipayBackSpareDOExample aExample = new AlipayBackSpareDOExample();
        List<String> tradeStatus = new ArrayList<>();
        tradeStatus.add(TRADE_SUCCESS);
        tradeStatus.add(TRADE_FINISH);
        aExample.createCriteria().andTradeStatusIn(tradeStatus).andStatusEqualTo((byte) 0);
        List<AlipayBackSpareDO> alipayBackSpareDOs = alipayBackSpareDOMapper.selectByExample(aExample);
        if (null != alipayBackSpareDOs && alipayBackSpareDOs.size() > 0) {
            for (AlipayBackSpareDO spareDO : alipayBackSpareDOs) {
                TradeExample tqExample = new TradeExample();
                tqExample.createCriteria().andTradeIdEqualTo(Long.valueOf(spareDO.getOutTradeNo()));
                Trade tradeToUpdate = null;
                List<Trade> tradeList = tradeMapper.selectByExample(tqExample);
                if (null != tradeList && tradeList.size() > 0) {
                    tradeToUpdate = tradeList.get(0);
                }
                //有需要更新的trade
                if (null != tradeToUpdate) {
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    def.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
                    TransactionStatus transactionStatus = transactionManager.getTransaction(def);
                    Profiler.enter("task: pay spare updates");
                    //设置订单状态 送水 家政 维修记录付款
                    Trade trade = new Trade();
                    trade.setPayment((long) (Double.valueOf(spareDO.getTotalFee()) * 100l));
                    trade.setAlipayNo(spareDO.getTrade_no());
                    trade.setLastUpdated(new Date());
                    trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                    trade.setCodStatus(Constants.CodStatus.SIGN_IN.getCodStatusId());
                    trade.setVersion(tradeToUpdate.getVersion() + 1l);
                    TradeExample tExample1 = new TradeExample();
                    tExample1.createCriteria().andTradeIdEqualTo(Long.valueOf(spareDO.getOutTradeNo())).andVersionEqualTo(tradeToUpdate.getVersion());
                    if (tradeMapper.updateByExampleSelective(trade, tExample1) < 1) {
                        logger.error("alipay call back -- update trade failed. out_trade_no:" + spareDO.getOutTradeNo() + ",tradeNo:" + spareDO.getTrade_no());
                        transactionManager.rollback(transactionStatus);
                        break;
                    }
                    //设置order状态
                    List<Order> orders = new ArrayList<Order>();
                    if (null != tradeToUpdate && tradeToUpdate.getOrders().length() > 0) {
                        for (String id : tradeToUpdate.getOrders().split(";")) {
                            OrderExample orderExample = new OrderExample();
                            orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
                            List<Order> tempOrders = orderMapper.selectByExample(orderExample);
                            if (null != orders && tempOrders.size() > 0) {
                                orders.add(tempOrders.get(0));
                            }
                        }
                        boolean updateStockFailed = false;
                        for (Order order : orders) {
                            Order tmpOrder = new Order();
                            tmpOrder.setId(order.getId());
                            tmpOrder.setLastUpdated(new Date());
                            tmpOrder.setPayment(order.getTotalFee());
                            tmpOrder.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                            OrderExample oExample = new OrderExample();
                            oExample.createCriteria().andIdEqualTo(order.getId());//.andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                            orderMapper.updateByPrimaryKeySelective(tmpOrder);
                            /*if(null != order){
                            	if(!updateStock2(order, transactionStatus, 1)){		//更新库存(1=减库存)
                    				updateStockFailed = true;
                    				break;
                    			}
                    		}*/
                        }
                        /*if (updateStockFailed) {
                            transactionManager.rollback(transactionStatus);
                            logger.error("update item stock in timer task failed.");
                            break;
                        }*/
                    }
                    //删除 spare
                    AlipayBackSpareDO alipayBackSpareDO = new AlipayBackSpareDO();
                    alipayBackSpareDO.setVersion(spareDO.getVersion() + 1l);
                    alipayBackSpareDO.setStatus((byte) -1);
                    AlipayBackSpareDOExample asExample = new AlipayBackSpareDOExample();
                    asExample.createCriteria().andIdEqualTo(spareDO.getId()).andVersionEqualTo(spareDO.getVersion());
                    if (alipayBackSpareDOMapper.updateByExampleSelective(alipayBackSpareDO, asExample) < 1) {
                        logger.error("update alipay back spare failed. id:" + spareDO.getId());
                        transactionManager.rollback(transactionStatus);
                        break;
                    }
                    transactionManager.commit(transactionStatus);
                    Profiler.release();
                }
            }
        }
        memcachedClient.set(TRADE_TASK_FLAG, TimeConstants.REDIS_EXPIRE_SECONDS_5, "1");

        //同步半价活动商品信息至缓存
        /*List<BannerViewDO> bannerViewDOs = bannerService.fetchBannersWithCache();
        if (null != bannerViewDOs && bannerViewDOs.size() > 0) {

        }*/
    }
    
    /**
     * 更新库存
     *
     * @param order
     * @return
     */

    private boolean updateStock(Order order) {
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
                    logger.error("update groupon stock quantity failed. itemId:" + itemId + ",num:" + num);
                    return false;
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
                        Item itemTmp = new Item();
                        itemTmp.setSoldQuantity((int) itemSoldQuantity);
                        itemTmp.setLastUpdated(new Date());
                        itemTmp.setVersion(item.getVersion() + 1l);
                        long itemNum = item.getNum();
                        itemNum = itemNum - num;
                        itemTmp.setNum((int) itemNum);
                        ItemExample iExample = new ItemExample();
                        iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                        if (itemMapper.updateByExampleSelective(itemTmp, iExample) < 1) {
                            logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                            return false;
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
                    Item itemTmp = new Item();
                    itemTmp.setLastUpdated(new Date());
                    itemTmp.setSoldQuantity((int) itemSoldQuantity);
                    long itemNum = item.getNum();
                    itemNum = itemNum - num;
                    itemTmp.setNum((int) itemNum);
                    itemTmp.setVersion(item.getVersion() + 1l);
                    ItemExample iExample = new ItemExample();
                    iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                    if (itemMapper.updateByExampleSelective(itemTmp, iExample) < 1) {
                        logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * 更新库存2
     *
     * @param type 如果type=2加库存,type=1或其它减库存
     * @param order
     * @return
     */
    public boolean updateStock2(Order order, TransactionStatus transactionStatus, Integer type) {
        //if (Long.compare(order.getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
            long itemId = order.getArtificialId();
            Integer num = (null == order.getNum() ? 0 : order.getNum());
            if(itemId < 1){
            	return false;
            }
            
            Map<String,Object> map = new HashMap<String, Object>();
            if(2 == type){		//如果type=2加库存,type=1或其它减库存
            	map.put("num", -num);
            }else{			//其它减库存
            	map.put("num", num);
            }
            
            if(null != order.getHasPanicTag() && order.getHasPanicTag().equals((byte)1)){
            	//HasPanicTag 是否有抢购标(0=没有;1=有)
            	List<Long> itemIds = new ArrayList<Long>();
                itemIds.add(itemId);
                List<ObjectTaggedDO> panicBuyingItemTags =  fetchPanicBuyingTagViaItemIds(itemIds, BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
                if(null != panicBuyingItemTags && !panicBuyingItemTags.isEmpty()){
                	ObjectTaggedDO objectTaggedDO = panicBuyingItemTags.get(0);
                	if(null != objectTaggedDO && null != objectTaggedDO.getId()){
                		map.put("objectTaggedId", objectTaggedDO.getId());
                		if(tradeMapper.updateObjectTaggedNumById(map) < 1){
                			logger.error("update ObjectTagged stock quantity failed. itemId:" + itemId + ",num:" + num);
                			if(null != transactionStatus){
                				transactionStatus.setRollbackOnly();
                			}
                			return false;
                		}
                	}
                }else{
                	if(null != type && type.equals(1)){
                		//如果购买商品减库存时无抢购标时，下单失败
                		return false;
                	}
                }
            }else{
            	map.put("itemId", itemId);
            	//正常商品(不是抢购标的)
            	GrouponDOExample gExample = new GrouponDOExample();
                gExample.createCriteria().andItemIdEqualTo(itemId).andStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus());
                gExample.setOrderByClause("online_end_time DESC");
                List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
                
                //是团购商品
                if (null != grouponDOs && grouponDOs.size() > 0) {
            		if(tradeMapper.updateGrouponItemNum(map) < 1){
            			logger.error("update groupon stock quantity failed. itemId:" + itemId + ",num:" + num);
            			if(null != transactionStatus){
            				transactionStatus.setRollbackOnly();
            			}
            			return false;
            		}else{
            			if (tradeMapper.updateItemNum(map) < 1) {
                        	logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                            //com.welink.web.common.filter.Profiler.release();
                        	if(null != transactionStatus){
                				transactionStatus.setRollbackOnly();
                			}
                            return false;
                        }
            		}
                }else{
                	if (tradeMapper.updateItemNum(map) < 1) {
                    	logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                    	if(null != transactionStatus){
            				transactionStatus.setRollbackOnly();
            			}
                        //com.welink.web.common.filter.Profiler.release();
                        return false;
                    }
                }
            }
        return true;
    }
    
    public List<Order> findOrdersByTradeId(Long tradeId){
    	checkNotNull(tradeId);
    	OrderExample orderExample = new OrderExample();
    	orderExample.createCriteria().andTradeIdEqualTo(tradeId).andArtificialIdGreaterThan(0L);
    	List<Order> orderList = orderMapper.selectByExample(orderExample);
    	return orderList;
    }
    
    /**
     * 根据商品ids 获取有开始结束时间的特定标记DO
     *
     * @param itemIds
     * @param tag
     * @return
     */
    private List<ObjectTaggedDO> fetchPanicBuyingTagViaItemIds(List<Long> itemIds, Long tag) {
        if (null != itemIds && itemIds.size() > 0) {
        	Date now = new Date();
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdIn(itemIds).andTagIdEqualTo(tagId).andStatusEqualTo((byte) 1)
                	.andStartTimeLessThanOrEqualTo(now).andEndTimeGreaterThan(now);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (null != objectTaggedDOs) {
                    return objectTaggedDOs;
                }
            }
        }
        return null;
    }
    
    /**
     * 
     * sureTrade:(确认订单确认收货). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param id
     * @return
     */
    public Boolean sureTrade(final Long id){
        	if(null == id || id < 0){
        		return false;
        	}
			Date nowDate = new Date();
			Date now7 =	TimeUtils.addDay(nowDate, 7); //加7天时间 
			//Trade trade = tradeMapper.selectByPrimaryKey(id);
			Trade trade = new Trade();
			trade.setId(id);
			trade.setStatus(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
			trade.setEndTime(nowDate);
			trade.setTimeoutActionTime(now7);
			tradeMapper.updateByPrimaryKeySelective(trade);
			
			if(null != trade && trade.getId() > 0){
				TradeCourierDOExample tradeCourierDOExample = new TradeCourierDOExample();
				tradeCourierDOExample.createCriteria().andTradeIdEqualTo(trade.getId()).andTypeEqualTo(1025).andStatusEqualTo(Byte.valueOf("1"));
				//tradeCourierDOMapper.selectByExample(tradeCourierDOExample);
				TradeCourierDO tradeCourierDO = new TradeCourierDO();
				tradeCourierDO.setType(1026);
				tradeCourierDO.setEndTime(nowDate);
				tradeCourierDOMapper.updateByExampleSelective(tradeCourierDO, tradeCourierDOExample);
			}
			return true;
	}

    /**
     * 时间向后推n天
     *
     * @param date
     * @param day
     * @return
     */

    public Date addDay(Date date, int day) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, day);//把日期往后增加day天.整数往后推,负数往前移动
        date = calendar.getTime();   //这个时间就是日期往后推day天的结果
        return date;
    }

    public ItemMapper getItemMapper() {
        return itemMapper;
    }

    public void setItemMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public GrouponDOMapper getGrouponDOMapper() {
        return grouponDOMapper;
    }

    public void setGrouponDOMapper(GrouponDOMapper grouponDOMapper) {
        this.grouponDOMapper = grouponDOMapper;
    }

    public TradeMapper getTradeMapper() {
        return tradeMapper;
    }

    public void setTradeMapper(TradeMapper tradeMapper) {
        this.tradeMapper = tradeMapper;
    }

    public PushService getPushService() {
        return pushService;
    }

    public void setPushService(PushService pushService) {
        this.pushService = pushService;
    }

    public OrderMapper getOrderMapper() {
        return orderMapper;
    }

    public void setOrderMapper(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    public EvalService getEvalService() {
        return evalService;
    }

    public void setEvalService(EvalService evalService) {
        this.evalService = evalService;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public AlipayBackSpareDOMapper getAlipayBackSpareDOMapper() {
        return alipayBackSpareDOMapper;
    }

    public void setAlipayBackSpareDOMapper(AlipayBackSpareDOMapper alipayBackSpareDOMapper) {
        this.alipayBackSpareDOMapper = alipayBackSpareDOMapper;
    }

    public MemcachedClient getMemcachedClient() {
        return memcachedClient;
    }

    public void setMemcachedClient(MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    public UsePromotionService getUsePromotionService() {
        return usePromotionService;
    }

    public void setUsePromotionService(UsePromotionService usePromotionService) {
        this.usePromotionService = usePromotionService;
    }

    public BannerService getBannerService() {
        return bannerService;
    }

    public void setBannerService(BannerService bannerService) {
        this.bannerService = bannerService;
    }

	public ItemService getItemService() {
		return itemService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public TradeService getTradeService() {
		return tradeService;
	}

	public void setTradeService(TradeService tradeService) {
		this.tradeService = tradeService;
	}

	public TradeCourierDOMapper getTradeCourierDOMapper() {
		return tradeCourierDOMapper;
	}

	public void setTradeCourierDOMapper(TradeCourierDOMapper tradeCourierDOMapper) {
		this.tradeCourierDOMapper = tradeCourierDOMapper;
	}

	public TagsDOMapper getTagsDOMapper() {
		return tagsDOMapper;
	}

	public void setTagsDOMapper(TagsDOMapper tagsDOMapper) {
		this.tagsDOMapper = tagsDOMapper;
	}

	public ObjectTaggedDOMapper getObjectTaggedDOMapper() {
		return objectTaggedDOMapper;
	}

	public void setObjectTaggedDOMapper(ObjectTaggedDOMapper objectTaggedDOMapper) {
		this.objectTaggedDOMapper = objectTaggedDOMapper;
	}

	public ProfitService getProfitService() {
		return profitService;
	}

	public void setProfitService(ProfitService profitService) {
		this.profitService = profitService;
	}
}
