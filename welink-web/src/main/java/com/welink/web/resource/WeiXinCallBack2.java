package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.mysql.jdbc.Connection;
import com.welink.biz.service.UsePromotionService;
import com.welink.biz.wx.tenpay.ResponseHandler;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.biz.wx.tenpay.util.WXUtil;
import com.welink.biz.wx.tenpay.util.XMLUtil;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.*;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.ons.TradeMessageProcessFacade;
import com.welink.web.ons.events.TradeEventType;

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
public class WeiXinCallBack2 {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(WeiXinCallBack2.class);

    @Resource
    private WeiXinBackDOMapper weiXinBackDOMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

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
    private UsePromotionService usePromotionService;

    @Resource
    private TradeMessageProcessFacade tradeMessageProcessFacade;

    @RequestMapping(value = {"/api/m/1.0/weiXinCallBack2.json", "/api/m/1.0/weiXinCallBack2.htm"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ResponseHandler resHandler = new ResponseHandler(request, response);
        resHandler.setKey(ConstantUtil.PARTNER_KEY);
        //商户订单号
        String out_trade_no = resHandler.getParameter("out_trade_no");
        out_trade_no = out_trade_no.split("_")[0];
        //财付通订单号
        String transaction_id = resHandler.getParameter("transaction_id");
        //金额,以分为单位
        String total_fee = resHandler.getParameter("total_fee");
        //如果有使用折扣券，discount有值，total_fee+discount=原请求的total_fee
        String discount = resHandler.getParameter("discount");
        //支付结果
        String trade_state = resHandler.getParameter("trade_state");
        String sign = resHandler.getParameter("sign");
        String trade_mode = resHandler.getParameter("trade_mode");
        String partner = resHandler.getParameter("partner");
        String bank_type = resHandler.getParameter("bank_type");
        String fee_type = resHandler.getParameter("fee_type");
        String attach = resHandler.getParameter("attach");
        String time_end = resHandler.getParameter("time_end");
        

        WeiXinBackDO weiXinBackDO = buildWeiXinCallBackDO(out_trade_no, trade_state, transaction_id, total_fee, sign, partner, bank_type, fee_type, attach, time_end);

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
//            resHandler.sendToCFT("fail");
            return JSON.toJSONString("fail");
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
            tradeInfo.setTradeStatus(Integer.valueOf(trade_state));
            boolean appointDeal = appointServiceDeal(tradeInfo);
            if (appointDeal) {
                usePromotionService.changePromotionFrozenToUsed(outTrade.getTradeId(), outTrade.getBuyerId());
//                resHandler.sendToCFT("success");
                return JSON.toJSONString("success");
            } else {
                //resHandler.sendToCFT("fail");
                return JSON.toJSONString("success");
            }
        } else {
            log.info("insert weixin call back msg failed. cant find trade. out_trade_no:" + weiXinBackDO.getOutTradeNo() + ",weixin tradeNo:" + weiXinBackDO.getTransactionId());
            //resHandler.sendToCFT("fail");
            return JSON.toJSONString("fail");
        }
//        return null;
    }

    private WeiXinBackDO buildWeiXinCallBackDO(String out_trade_no, String trade_status, String transaction_id, String total_fee, String sign, String partner, String bank_type, String fee_type, String attach, String time_end) {
        WeiXinBackDO weiXinBackDO = new WeiXinBackDO();
        weiXinBackDO.setOutTradeNo(out_trade_no);
        weiXinBackDO.setTransactionId(transaction_id);
        if (StringUtils.isNotBlank(total_fee)) {
            weiXinBackDO.setTotalFee(Integer.valueOf(total_fee));
        }
        if ("0".equals(trade_status)) {
            weiXinBackDO.setResultCode("SUCCESS");
            weiXinBackDO.setReturnCode("SUCCESS");
        }
        weiXinBackDO.setTradeType("APP");
        weiXinBackDO.setDateCreated(new Date());
        weiXinBackDO.setAttach(attach);
        weiXinBackDO.setBankType(bank_type);
        weiXinBackDO.setFeeType(fee_type);
        weiXinBackDO.setLastUpdated(new Date());
        weiXinBackDO.setMchId(partner);
        weiXinBackDO.setSign(sign);
        weiXinBackDO.setTimeEnd(time_end);
        return weiXinBackDO;

    }

    private boolean appointServiceDeal(final TradeInfo tradeInfo) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("change-trade-order-status");
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        transactionTemplate.execute(new TransactionCallback<TradeInfo>() {
            @Override
            public TradeInfo doInTransaction(TransactionStatus transactionStatus) {
                //获取参数
                if (tradeInfo.getTradeStatus() == 0) {
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
                            if (tradeInfo.getTradeStatus() == 0) {
                                tmpOrder.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                                tmpOrder.setVersion(order.getVersion() + 1l);
                                OrderExample oExample = new OrderExample();
                                oExample.createCriteria().andIdEqualTo(order.getId()).andStatusEqualTo((byte) 2);
                                orderMapper.updateByExampleSelective(tmpOrder, oExample);
                                //更新库存和销售量 针对非物流订单
                                updateStock(order, transactionStatus);

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
        int tradeStatus;


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

        public int getTradeStatus() {
            return tradeStatus;
        }

        public void setTradeStatus(int tradeStatus) {
            this.tradeStatus = tradeStatus;
        }
    }

    public static void main(String[] args) {
        String s = "0.01";
        double d = Double.valueOf(s);
        long l = Long.valueOf((long) (d * 10000l));
        System.out.println(l);
        
        for(int j = 0; j< 5; j++){
        	System.out.println((int)((Math.random()*9+1)*10000));
        }
        String out_trade_no = "7444525547462556unes"+(int)((Math.random()*9+1)*10000);
        String out_trade_no2 = out_trade_no.split("unes")[0];
        System.out.println(out_trade_no+"....."+out_trade_no2+"--------------"+out_trade_no.split("unes")[1]);
    }
}
