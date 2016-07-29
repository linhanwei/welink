package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;
import com.mysql.jdbc.Connection;
import com.welink.biz.profit.AddProfitImpl;
import com.welink.biz.service.GivePresentService;
import com.welink.biz.service.MikuUserAgencyService;
import com.welink.biz.service.UsePromotionService;
import com.welink.biz.wx.tenpay.util.PayCommonUtil;
import com.welink.biz.wx.tenpay.util.XMLUtil;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.*;
import com.welink.commons.events.ProfitEvent;
import com.welink.commons.events.WechatCallbackEvent;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 14-12-8.
 */
@RestController
public class HWeiXinCallBack {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(HWeiXinCallBack.class);
    
    public static final String HWEIXINCALLBACK_FLAG = "hWeiXinCallBack_flag";

    @Resource
    private WeiXinBackDOMapper weiXinBackDOMapper;

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
    private AsyncEventBus asyncEventBus;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private UsePromotionService usePromotionService;
    
    @Resource
    private MikuUserAgencyService mikuUserAgencyService;
    
    @Resource
    private GivePresentService givePresentService;
    
    @Resource
    private MikuScratchCardDOMapper mikuScratchCardDOMapper;
    
    @Resource
    private AddProfitImpl addProfitImpl;

    @Resource
    private TradeMessageProcessFacade tradeMessageProcessFacade;
    @RequestMapping(value = {"/api/h/1.0/hWeiXinCallBack.json", "/api/h/1.0/hWeiXinCallBack.htm"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {

        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        String result = new String(outSteam.toByteArray(), "utf-8");
        Map<String, String> map = XMLUtil.doXMLParse(result);

        WeiXinBackDO weiXinBackDO = buildWeiXinCallBackDO(map);
        
        /*Object oFlag = memcachedClient.get(HWEIXINCALLBACK_FLAG+weiXinBackDO.getOutTradeNo());
		if (null == oFlag || (null != oFlag && StringUtils.equals("1", oFlag.toString()))) {
            memcachedClient.set(HWEIXINCALLBACK_FLAG+weiXinBackDO.getOutTradeNo(), 2 * 60, "0");
        } else if (null != oFlag && StringUtils.equals("0", oFlag.toString())) {
        	return JSON.toJSONString(PayCommonUtil.setXML("FAIL", ""));
        }*/

        String mobile = "";
        try {
            TradeExample tradeExample = new TradeExample();
            tradeExample.createCriteria().andTradeIdEqualTo(Long.parseLong(weiXinBackDO.getOutTradeNo()));
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
        }

        EventTracker.track(mobile, "trade", "callback-pre", weiXinBackDO.getOutTradeNo(), 1L);
        //存储call back 信息 存储失败
        if (storeWeiXinBackInfo(weiXinBackDO)) {
            log.error("存储微信返回的数据失败，要存储的weiXinBackDO=" + weiXinBackDO);
//            response.getWriter().write(PayCommonUtil.setXML("FAIL", ""));
            return JSON.toJSONString(PayCommonUtil.setXML("FAIL", ""));
        }

        com.welink.web.common.filter.Profiler.enter("weixin callback transaction");
        long tradeId = Long.valueOf(weiXinBackDO.getOutTradeNo());
        TradeExample tExample = new TradeExample();
        tExample.createCriteria().andTradeIdEqualTo(tradeId);
        List<Trade> outTrades = tradeMapper.selectByExample(tExample);
        Trade outTrade = null;
        if (null != outTrades && outTrades.size() > 0) {
            outTrade = outTrades.get(0);
        } else {
            String msg = "outTradeNO:" + weiXinBackDO.getOutTradeNo() + " buyerId:" + weiXinBackDO.getOpenid();
            log.error("没有查到指定订单msg=" + msg);
        }

        if (null != outTrade) {
            TradeInfo tradeInfo = new TradeInfo();
            tradeInfo.setTrade(outTrade);
            tradeInfo.setWeiXinBackDO(weiXinBackDO);
            tradeInfo.setChangeStatus(false);
            boolean appointDeal = appointServiceDeal(tradeInfo, outTrade);
            if (appointDeal) {
            	if (null != outTrade.getTradeId() && outTrade.getTradeId() > 0) {	//交易分润
                	asyncEventBus.post(new ProfitEvent(outTrade.getTradeId(), 1));	//通过事件总线 交易分润 //1=加分润；2=减分润
            	}
                usePromotionService.changePromotionFrozenToUsed(outTrade.getTradeId(), outTrade.getBuyerId());
                asyncEventBus.post(new WechatCallbackEvent(weiXinBackDO));
                //赠品操作
                givePresentService.giveOnePresentToConsumerStander(outTrade.getTradeId(), outTrade.getBuyerId());
//                response.getWriter().write(PayCommonUtil.setXML("SUCCESS", "OK"));
                return JSON.toJSONString(PayCommonUtil.setXML("SUCCESS", "OK"));
            } else {
//                response.getWriter().write(PayCommonUtil.setXML("FAIL", ""));
                return JSON.toJSONString(PayCommonUtil.setXML("FAIL", ""));
            }
        } else {
            log.info("insert weixin call back msg failed. cant find trade. out_trade_no:" + weiXinBackDO.getOutTradeNo() + ",weixin tradeNo:" + weiXinBackDO.getTransactionId());
//            response.getWriter().write(PayCommonUtil.setXML("FAIL", ""));
            return JSON.toJSONString(PayCommonUtil.setXML("FAIL", ""));
        }
    }

    private WeiXinBackDO buildWeiXinCallBackDO(Map<String, String> map) {
        WeiXinBackDO weiXinBackDO = new WeiXinBackDO();
        weiXinBackDO.setAppId(map.get("appid"));
        weiXinBackDO.setMchId(map.get("mch_id"));
        weiXinBackDO.setDeviceInfo(map.get("device_info"));
        weiXinBackDO.setNonceStr(map.get("nonce_str"));
        weiXinBackDO.setSign(map.get("sign"));
        weiXinBackDO.setResultCode(map.get("result_code"));
        weiXinBackDO.setErrCode(map.get("err_code"));
        weiXinBackDO.setErrCodeDes(map.get("err_code_des"));
        weiXinBackDO.setOpenid(map.get("openid"));
        weiXinBackDO.setIsSubscribe(map.get("is_subscribe"));
        weiXinBackDO.setTradeType(map.get("trade_type"));
        weiXinBackDO.setBankType(map.get("bank_type"));
        if (StringUtils.isNotBlank(map.get("total_fee"))) {
            weiXinBackDO.setTotalFee(Integer.valueOf(map.get("total_fee")));
        }
        weiXinBackDO.setFeeType(map.get("fee_type"));
        if (StringUtils.isNotBlank(map.get("cash_fee"))) {
            weiXinBackDO.setCashFee(Integer.valueOf(map.get("cash_fee")));
        }
        weiXinBackDO.setCashFeeType(map.get("cash_fee_type"));
        if (StringUtils.isNotBlank(map.get("coupon_fee"))) {
            weiXinBackDO.setCouponFee(Integer.valueOf(map.get("coupon_fee")));
        }
        if (StringUtils.isNotBlank(map.get("coupon_count"))) {
            weiXinBackDO.setCouponCount(Integer.valueOf(map.get("coupon_count")));
        }
        weiXinBackDO.setCouponBatchIdn(map.get("coupon_batch_id_$n"));
        weiXinBackDO.setCouponBatchIdn(map.get("coupon_id_$n"));

        if (StringUtils.isNotBlank(map.get("coupon_fee_$n"))) {
            weiXinBackDO.setCouponFeen(Integer.valueOf(map.get("coupon_fee_$n")));
        }
        weiXinBackDO.setTransactionId(map.get("transaction_id"));
        weiXinBackDO.setOutTradeNo(map.get("out_trade_no"));
        weiXinBackDO.setAttach(map.get("attach"));
        weiXinBackDO.setTimeEnd(map.get("time_end"));
        weiXinBackDO.setDateCreated(new Date());
        weiXinBackDO.setLastUpdated(new Date());
        weiXinBackDO.setReturnCode(map.get("return_code"));
        weiXinBackDO.setReturnMsg(map.get("return_msg"));
        return weiXinBackDO;

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
                if (StringUtils.equals(tradeInfo.getWeiXinBackDO().getReturnCode(), "SUCCESS")) {
                	Trade trade2 = tradeInfo.getTrade();
                	if(null != trade2 && null != trade2.getBuyerId() 
                			&& trade2.getType().equals(Constants.TradeType.join_agency.getTradeTypeId())){
                		//当为成为代理订单时更新购买者为代理
                		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(trade2.getBuyerId());
                		if(null != profileDO && profileDO.getIsAgency().equals((byte)0)){
                			profileDO.setIsAgency((byte)1);	//1=代理
                			profileDO.setLastUpdated(new Date());
                			if(profileDOMapper.updateByPrimaryKey(profileDO) < 1){
                				transactionStatus.setRollbackOnly();
                				tradeInfo.setChangeStatus(false);
                				return tradeInfo;
                			}
                		}
                		//建立代理关系
    		        	int insertMikuUserAgency = mikuUserAgencyService.updateMikuUserAgency(trade2.getpUserId(), trade2.getBuyerId(), transactionStatus);	
    		        	if(insertMikuUserAgency < 1){
    		        		transactionStatus.setRollbackOnly();
            				tradeInfo.setChangeStatus(false);
            				return tradeInfo;
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
                	
                    //设置订单状态 送水 家政 维修记录付款
                    Trade trade = new Trade();
                    trade.setPayment(Long.valueOf(tradeInfo.getWeiXinBackDO().getTotalFee()));
                    trade.setPayType(Constants.PayType.ONLINE_WXPAY.getPayTypeId());
                    trade.setAlipayNo(tradeInfo.getWeiXinBackDO().getTransactionId());
                    trade.setLastUpdated(new Date());
                    trade.setPayTime(new Date());
                    trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                    trade.setCodStatus(Constants.CodStatus.SIGN_IN.getCodStatusId());
                    trade.setVersion(tradeInfo.getTrade().getVersion() + 1l);
                    TradeExample tExample = new TradeExample();
                    List<Byte> toUpdateStatus = new ArrayList<>();
                    toUpdateStatus.add((byte) 2);
                    toUpdateStatus.add((byte) 9);
                    tExample.createCriteria().andTradeIdEqualTo(Long.valueOf(tradeInfo.getWeiXinBackDO().getOutTradeNo())).andVersionEqualTo(tradeInfo.getTrade().getVersion())
                            .andStatusIn(toUpdateStatus);
                    if (tradeMapper.updateByExampleSelective(trade, tExample) < 1) {
                        log.error("weixin pay . update trade status failed. trade id:" + trade.getTradeId());
                        return tradeInfo;
                    }else {
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
                            if (StringUtils.equals(tradeInfo.getWeiXinBackDO().getReturnCode(), "SUCCESS") && StringUtils.equals(tradeInfo.getWeiXinBackDO().getResultCode(), "SUCCESS")) {
                                tmpOrder.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                                tmpOrder.setVersion(order.getVersion() + 1l);
                                OrderExample oExample = new OrderExample();
                                oExample.createCriteria().andIdEqualTo(order.getId()).andStatusEqualTo((byte) 2);
                                orderMapper.updateByExampleSelective(tmpOrder, oExample);
                                //更新库存和销售量 针对非物流订单
                                //updateStock(order, transactionStatus);

                            } else {
                                tmpOrder.setVersion(order.getVersion() + 1l);
                                OrderExample oExample = new OrderExample();
                                oExample.createCriteria().andIdEqualTo(order.getId()).andStatusEqualTo((byte) 4);
                                orderMapper.updateByExampleSelective(tmpOrder, oExample);
                            }
                        }
                    }
                }
                tradeInfo.setChangeStatus(true);
                return tradeInfo;
            }
        });
        return tradeInfo.isChangeStatus();
    }

    /**
     * 存储微信回调信息
     *
     * @param weiXinBackDO
     * @return
     */
    private boolean storeWeiXinBackInfo(WeiXinBackDO weiXinBackDO) {
        //存储更新
        if (weiXinBackDOMapper.insertSelective(weiXinBackDO) < 0) {
            log.error("insert weixin call back msg failed. out_trade_no:" + weiXinBackDO.getOutTradeNo() + ",weixin tradeNo:" + weiXinBackDO.getTransactionId());
            return true;
        }
        return false;
    }


    /**
     * 更新库存
     *
     * @param order
     * @return
     */
    private boolean updateStock(Order order, TransactionStatus transactionStatus) {
        if (null != order.getCategoryId() && Long.compare(order.getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
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
                    com.welink.commons.domain.Item item = itemMapper.selectByPrimaryKey(itemId);
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
                        com.welink.commons.domain.Item item1 = new com.welink.commons.domain.Item();
                        item1.setSoldQuantity((int) itemSoldQuantity);
                        long itemNum = item.getNum();
                        itemNum = itemNum - num;
                        item1.setNum((int) itemNum);
                        item1.setVersion(item.getVersion() + 1l);
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
                com.welink.commons.domain.Item item = itemMapper.selectByPrimaryKey(itemId);
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
                        return false;
                    }
                }
            }
        }
        return true;
    }

    class TradeInfo {
        Trade trade;
        WeiXinBackDO weiXinBackDO;
        boolean changeStatus;

        public Trade getTrade() {
            return trade;
        }

        public void setTrade(Trade trade) {
            this.trade = trade;
        }

        public WeiXinBackDO getWeiXinBackDO() {
            return weiXinBackDO;
        }

        public void setWeiXinBackDO(WeiXinBackDO weiXinBackDO) {
            this.weiXinBackDO = weiXinBackDO;
        }

        public boolean isChangeStatus() {
            return changeStatus;
        }

        public void setChangeStatus(boolean changeStatus) {
            this.changeStatus = changeStatus;
        }
    }

    public static void main(String[] args) {
        String s = "0.01";
        double d = Double.valueOf(s);
        long l = Long.valueOf((long) (d * 10000l));
        System.out.println(l);
    }
}
