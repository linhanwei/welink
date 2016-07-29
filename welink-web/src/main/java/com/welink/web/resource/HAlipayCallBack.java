package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;
import com.mysql.jdbc.Connection;
import com.welink.biz.common.MSG.SMSUtils;
import com.welink.biz.common.pay.AlipayConfig;
import com.welink.biz.common.pay.AlipayNotify;
import com.welink.biz.service.UsePromotionService;
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

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
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
 * Created by daniel on 14-12-8.
 */
@RestController
public class HAlipayCallBack {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(HAlipayCallBack.class);

    private static final String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    private static final String TRADE_FINISH = "TRADE_FINISHED";

    @Resource
    private AlipayBackDOMapper alipayBackDOMapper;

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
    private AlipayBackSpareDOMapper alipayBackSpareDOMapper;

    @Resource
    private UsePromotionService usePromotionService;

    @Resource
    private TradeMessageProcessFacade tradeMessageProcessFacade;
    
    @Resource
    private AsyncEventBus asyncEventBus;

    @RequestMapping(value = {"/api/h/1.0/hAlipayCallBack.json", "api/h/1.0/hAlipayCallBack.htm"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        //RSA签名解密
        if (AlipayConfig.wap_sign_type.equals("0001")) {
            params = AlipayNotify.decrypt(params);
        }
        //获取参数
        //XML解析notify_data数据
        Document doc_notify_data = DocumentHelper.parseText(params.get("notify_data"));
        String sign = params.get("sign");
        //商户订单号
        String out_trade_no = doc_notify_data.selectSingleNode("//notify/out_trade_no").getText();
        //支付宝交易号
        String trade_no = doc_notify_data.selectSingleNode("//notify/trade_no").getText();
        //交易状态
        String trade_status = doc_notify_data.selectSingleNode("//notify/trade_status").getText();
        String payment_type = doc_notify_data.selectSingleNode("//notify/payment_type").getText();
        String subject = doc_notify_data.selectSingleNode("//notify/subject").getText();
        String buyer_email = doc_notify_data.selectSingleNode("//notify/buyer_email").getText();
        String gmt_create = doc_notify_data.selectSingleNode("//notify/gmt_create").getText();
        String notify_type = doc_notify_data.selectSingleNode("//notify/notify_type").getText();
        String quantity = doc_notify_data.selectSingleNode("//notify/quantity").getText();
        String notify_time = doc_notify_data.selectSingleNode("//notify/notify_time").getText();
        String seller_id = doc_notify_data.selectSingleNode("//notify/seller_id").getText();
        String is_total_fee_adjust = doc_notify_data.selectSingleNode("//notify/is_total_fee_adjust").getText();
        String total_fee = doc_notify_data.selectSingleNode("//notify/total_fee").getText();
        String gmt_payment = doc_notify_data.selectSingleNode("//notify/gmt_payment").getText();
        String seller_email = doc_notify_data.selectSingleNode("//notify/seller_email").getText();
        String price = doc_notify_data.selectSingleNode("//notify/price").getText();
        String notify_id = doc_notify_data.selectSingleNode("//notify/notify_id").getText();
        String use_coupon = doc_notify_data.selectSingleNode("//notify/use_coupon").getText();
        String buyer_id = doc_notify_data.selectSingleNode("//notify/buyer_id").getText();
        String sign_type = AlipayConfig.wap_sign_type;
        final AlipayBackDO callBackDO = new AlipayBackDO();
        log.info("out trade no :" + trade_no + ", tradestatus:" + trade_status);

        //building call back do
        buildCallBackDO(sign, out_trade_no, trade_no, trade_status, payment_type, subject, buyer_email, gmt_create, notify_type, quantity, notify_time, seller_id, is_total_fee_adjust, total_fee, gmt_payment, seller_email, price, notify_id, use_coupon, buyer_id, sign_type, callBackDO);

        String mobile = "";
        try {
            TradeExample tradeExample = new TradeExample();
            tradeExample.createCriteria().andTradeIdEqualTo(Long.parseLong(callBackDO.getOutTradeNo()));
            List<Trade> trades = tradeMapper.selectByExample(tradeExample);
            Preconditions.checkArgument(trades.size() == 1);

            ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(trades.get(0).getBuyerId());

            if (null != profileDO) {
                mobile = profileDO.getMobile();
                EventTracker.track(mobile, "halipayback", "callback", trade_no + "," + trade_status, 1L);
            }

            Preconditions.checkArgument(StringUtils.isNotBlank(mobile), "the profile id [{}] with no mobile, WTF", profileDO.getId());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("fetch profile error. ");
        }
        EventTracker.track(callBackDO.getBuyerId(), "trade", "callback-pre", callBackDO.getTradeStatus(), 1L);
        EventTracker.track(callBackDO.getOutTradeNo(), "trade", "callback-pre", callBackDO.getTradeStatus(), 1L);
        EventTracker.track(callBackDO.getBuyerId(), "trade", "callback-pre", callBackDO.getOutTradeNo(), 1L);
        EventTracker.track(mobile, "trade", "callback-pre", callBackDO.getOutTradeNo(), 1L);
        log.info("alipay callback out trade no.:" + callBackDO.getOutTradeNo() + ",status:" + callBackDO.getTradeStatus());
        String result = "failed";
        if (StringUtils.equalsIgnoreCase(callBackDO.getTradeStatus(), "TRADE_FINISHED")) {
            result = "success";
            log.info("trade finished return......alipay callback out trade no.:" + callBackDO.getOutTradeNo() + ",status:" + callBackDO.getTradeStatus());
            return JSON.toJSONString(result);
        }
        //存储call back 信息 存储失败
        if (storeAlipayBackInfo(callBackDO)) {
            result = "failed";
            return JSON.toJSONString(result);
        }

        if (AlipayNotify.verifyForH5(params)) {//验证成功
            //——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
            //TRADE_FINISHED 不处理
            if (trade_status.equals("TRADE_FINISHED")) {
//                com.welink.web.common.filter.Profiler.enter("alipay callback transaction");
//                long tradeId = Long.valueOf(callBackDO.getOutTradeNo());
//                TradeExample tExample = new TradeExample();
//                tExample.createCriteria().andTradeIdEqualTo(tradeId);
//                List<Trade> outTrades = tradeMapper.selectByExample(tExample);
//                Trade outTrade = null;
//                if (null != outTrades && outTrades.size() > 0) {
//                    outTrade = outTrades.get(0);
//                } else {
//                    String msg = "outTradeNO:" + callBackDO.getOutTradeNo() + " buyerId:" + callBackDO.getBuyerId();
//                    SMSUtils.sendSmsForAlipayBack(msg, "18605816526");
//                }
//
//                if (null != outTrade) {
//                    TradeInfo tradeInfo = new TradeInfo();
//                    tradeInfo.setTrade(outTrade);
//                    tradeInfo.setAlipayBackDO(callBackDO);
//                    tradeInfo.setChangeStatus(false);
//
//                    boolean appointDeal = appointServiceDeal(tradeInfo);
//                    if (appointDeal) {
//                        usePromotionService.changePromotionFrozenToUsed(outTrade.getTradeId(), outTrade.getBuyerId());
//                        result = "success";
//                    } else {
//                        result = "failed";
//                    }
//                } else {
//                    log.info("insert alipay call back msg failed. cant find trade. out_trade_no:" + callBackDO.getOutTradeNo() + ",tradeNo:" + callBackDO.getTrade_no());
//                    result = "failed";
//                    return JSON.toJSONString(result);
//                }
            } else if (trade_status.equals("TRADE_SUCCESS")) {
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //如果有做过处理，不执行商户的业务程序
                //注意：
                //该种交易状态只在一种情况下出现——开通了高级即时到账，买家付款成功后。
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
                    SMSUtils.sendSmsForAlipayBack(msg, "18605816526");
                }

                if (null != outTrade) {
                    TradeInfo tradeInfo = new TradeInfo();
                    tradeInfo.setTrade(outTrade);
                    tradeInfo.setAlipayBackDO(callBackDO);
                    tradeInfo.setChangeStatus(false);
                    boolean appointDeal = appointServiceDeal(tradeInfo);
                    if (appointDeal) {
                        usePromotionService.changePromotionFrozenToUsed(outTrade.getTradeId(), outTrade.getBuyerId());
                        if (null != outTrade.getTradeId() && outTrade.getTradeId() > 0) {	//交易分润
                        	asyncEventBus.post(new ProfitEvent(outTrade.getTradeId(), 1));	//通过事件总线 交易分润 //1=加分润；2=减分润
                        	//profitService.profitByTradeId(tradeId);
                		}
                        result = "success";
                    } else {
                        result = "failed";
                    }
                } else {
                    log.info("insert alipay call back msg failed. cant find trade. out_trade_no:" + callBackDO.getOutTradeNo() + ",tradeNo:" + callBackDO.getTrade_no());
                    result = "failed";
                    return JSON.toJSONString(result);
                }
            }
        } else {//验证失败
            if (!storeAlipayBackSpareInfo(callBackDO)) {
                log.info("insert alipay call back spare msg failed. verify failed. out_trade_no:" + callBackDO.getOutTradeNo() + ",tradeNo:" + callBackDO.getTrade_no());
                result = "failed";
                return JSON.toJSONString(result);
            } else {
                log.info("insert alipay call back spare msg failed. verify failed. out_trade_no:" + callBackDO.getOutTradeNo() + ",tradeNo:" + callBackDO.getTrade_no());
                result = "success";
                return JSON.toJSONString(result);
            }
        }

        EventTracker.track(callBackDO.getBuyerId(), "trade", "callback-success", callBackDO.getTradeStatus(), 1L);
        EventTracker.track(callBackDO.getOutTradeNo(), "trade", "callback-success", callBackDO.getTradeStatus(), 1L);
        EventTracker.track(callBackDO.getBuyerId(), "trade", "callback-success", callBackDO.getOutTradeNo(), 1L);
        EventTracker.track(mobile, "trade", "callback-success", callBackDO.getOutTradeNo(), 1L);
        result = "success";
        return JSON.toJSONString(result);
    }

    private void buildCallBackDO(String sign, String out_trade_no, String trade_no, String trade_status, String payment_type, String subject, String buyer_email, String gmt_create, String notify_type, String quantity, String notify_time, String seller_id, String is_total_fee_adjust, String total_fee, String gmt_payment, String seller_email, String price, String notify_id, String use_coupon, String buyer_id, String sign_type, AlipayBackDO callBackDO) {
        callBackDO.setSign(sign);
        callBackDO.setDateCreated(new Date());
        callBackDO.setGmtCreate(new Date());
        callBackDO.setOutTradeNo(out_trade_no);
        callBackDO.setTrade_no(trade_no);
        callBackDO.setTradeStatus(trade_status);
        callBackDO.setPaymentType(payment_type);
        callBackDO.setSubject(subject);
        callBackDO.setBuyerEmail(buyer_email);
        callBackDO.setBuyerId(buyer_id);
        if (StringUtils.isNotBlank(gmt_create)) {
            try {
                callBackDO.setGmtCreate(TimeUtil.str2DateTime(gmt_create));
            } catch (Exception e) {
                log.error("alipay call back parse gmt_create error. gmt_create:" + gmt_create);
            }
        }
        callBackDO.setNotifyType(notify_type);
        callBackDO.setQuantity(quantity);
        callBackDO.setSellerId(seller_id);
        callBackDO.setIsTotalFeeAdjust(is_total_fee_adjust);
        callBackDO.setTotalFee(total_fee);
        if (StringUtils.isNotBlank(notify_time)) {
            try {
                callBackDO.setNotifyTime(TimeUtil.str2DateTime(notify_time));
            } catch (Exception e) {
                log.error("alipay call back parse notify_time error. notify_time:" + notify_time);
            }
        }
        callBackDO.setSignType(sign_type);
        callBackDO.setUseCoupon(use_coupon);
        callBackDO.setNotifyId(notify_id);
        callBackDO.setPrice(price);
        callBackDO.setSellerEmail(seller_email);
        if (StringUtils.isNotBlank(gmt_payment)) {
            try {
                callBackDO.setGmtPayment(TimeUtil.str2DateTime(gmt_payment));
            } catch (Exception e) {
                log.error("alipay call back parse gmt_payment error. gmt_payment:" + gmt_payment);
            }
        }
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
                if (StringUtils.equals(tradeInfo.getAlipayBackDO().getTradeStatus(), TRADE_SUCCESS)) {
                    //设置订单状态
                    Trade trade = new Trade();
                    trade.setPayment(TradeUtil.getLongPrice(tradeInfo.getAlipayBackDO().getTotalFee()));
                    trade.setPayType(Constants.PayType.ONLINE_ALIPAY.getPayTypeId());
                    trade.setAlipayNo(tradeInfo.getAlipayBackDO().getTrade_no());
                    trade.setLastUpdated(new Date());
                    trade.setPayTime(new Date());
                    trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
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
                        return false;
                    }
                }
            }
        }
        return true;
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

    public static void main(String[] args) {
        String s = "0.01";
        double d = Double.valueOf(s);
        long l = Long.valueOf((long) (d * 10000l));
        System.out.println(l);
    }
}
