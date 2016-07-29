package com.welink.web.resource;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.OrderViewDO;
import com.welink.biz.common.model.TradeViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.pay.AlipayConfig;
import com.welink.biz.common.pay.AlipayCore;
import com.welink.biz.common.pay.RSA;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.AddressService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.StringUtil;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.EmployeeDO;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.LogisticsDO;
import com.welink.commons.domain.MikuCrowdfundDOExample;
import com.welink.commons.domain.MikuCrowdfundDOExample.Criteria;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderEvaluateDO;
import com.welink.commons.domain.OrderEvaluateDOExample;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.ShopDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.CommunityDOMapper;
import com.welink.commons.persistence.MikuCrowdfundDOMapper;
import com.welink.commons.persistence.OrderEvaluateDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.util.ParameterUtil;

/**
 * 返回订单信息，包括重新支付需要的支付宝信息(如果未付款)
 * totalFee是用户需要付款的钱数,包含运费,减去优惠和积分
 * price是商品的钱数
 * Created by daniel on 14-9-17.
 */
@RestController
public class FetchTrade {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FetchTrade.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private CommunityDOMapper communityDOMapper;

    @Resource
    private ShopService shopService;

    @Resource
    private AddressService addressService;

    @Resource
    private OrderEvaluateDOMapper orderEvaluateDOMapper;
    
    @Resource
    private MikuCrowdfundDOMapper mikuCrowdfundDOMapper;
    
    @Resource
    private Env env;

    @Resource
    private ItemService itemService;

    public static String MOBILE = "4006831717";

    public static String NAME = "米酷专业配送";

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/fetchTrade.json", "/api/h/1.0/fetchTrade.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //get parameters
        long tradeId = ParameterUtil.getParameterAslongForSpringMVC(request, "tradeId", -1l);
        long profileId = -1;
        long shopId = -1;
        long totalPrice = 0l;
        String title = "";
        String body = "";
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        Map<String, String> paraMap = new HashMap<>();

        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
        //fetch order info
        TradeExample tradeExample = new TradeExample();
        tradeExample.createCriteria().andTradeIdEqualTo(tradeId).andBuyerIdEqualTo(profileId);
        byte tradeStatus = -1;
        BaseResult<List<Trade>> tradeList = appointmentService.findByExample(tradeExample);
        Trade trade = null;
        if (null != tradeList && tradeList.getResult() != null && tradeList.getResult().size() > 0) {
            trade = tradeList.getResult().get(0);
            tradeStatus = trade.getStatus();
            resultMap.put("region", BizConstants.APPOINT_DELIVERY_REGION);
            shopId = trade.getShopId();
            totalPrice = trade.getTotalFee();
            title = trade.getTitle();

            //查找站點
            Long communityId = trade.getCommunityId();
            CommunityDO communityDO = communityDOMapper.selectByPrimaryKey(communityId);

            TradeViewDO tradeViewDO = ViewDOCopy.buildTradeViewDO(trade, communityDO);
            Long courierId = trade.getCourier();
            if (null != courierId && trade.getShippingType() != 1) {
                EmployeeDO employeeDO = new EmployeeDO();// = employeeDOMapper.selectByPrimaryKey(courierId);
                if (null != employeeDO && null != employeeDO.getMobile()) {
                    tradeViewDO.setDeliverMobile(MOBILE);
                }
                if (null != employeeDO && null != employeeDO.getUserName()) {
                    tradeViewDO.setDeliverName(NAME);
                }
            }

            List<Order> orders = new ArrayList<Order>();
            List<Long> itemIds = Lists.newArrayList();
            if (trade.getOrders().length() > 0) {
                for (String id : trade.getOrders().split(";")) {
                    OrderExample orderExample = new OrderExample();
                    orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
                    List<Order> tempOrders = orderMapper.selectByExample(orderExample);
                    if (null != tempOrders && tempOrders.size() > 0) {
                        if (null != tempOrders.get(0).getCategoryId() && Long.compare(tempOrders.get(0).getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
                            orders.add(tempOrders.get(0));
                            body += tempOrders.get(0).getTitle() + "-" + tempOrders.get(0).getId() + "-";
                            itemIds.add(tempOrders.get(0).getArtificialId());
                        }else{
                        	resultMap.put("postFee", tempOrders.get(0).getPrice());
                        }
                    }
                }
            }
            Map<Long, Item> itemMap = new HashMap<>();
            BaseResult<List<Item>> itemsResult = itemService.fetchItemsByItemIds(itemIds);
            if (null != itemsResult && itemsResult.isSuccess() && itemsResult.getResult() != null) {
                for (Item item : itemsResult.getResult()) {
                    itemMap.put(item.getId(), item);
                }
            }
            List<OrderViewDO> orderViewDOs = Lists.newArrayList();
            for (Order order : orders) {
                OrderViewDO orderViewDO = ViewDOCopy.buildOrderViewDO(order);
                if (null != itemMap.get(order.getArtificialId())) {
                    orderViewDO.setSpecification(itemMap.get(order.getArtificialId()).getSpecification());
                }
                orderViewDOs.add(orderViewDO);

            }
            tradeViewDO.setOrderViewDOs(orderViewDOs);

            long consigneeId = trade.getConsigneeId();
            LogisticsDO logisticsDO = addressService.fetchLogisticsByConsigneeId(consigneeId);
            resultMap.put("logisticsDO", ViewDOCopy.buildLogisticsViewDO(logisticsDO));

            //评论
            if (trade.getStatus() == Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId()) {
                List<Long> ratedTradeIds = new ArrayList<>();
                OrderEvaluateDOExample oExample = new OrderEvaluateDOExample();
                List<Long> rateUsers = new ArrayList<>();
                rateUsers.add(profileId);
                rateUsers.add(BizConstants.WELINK_RATE_ID);
                oExample.createCriteria().andTradeIdEqualTo(trade.getTradeId()).andUserIdIn(rateUsers);
                List<OrderEvaluateDO> orderEvaluateDOs = orderEvaluateDOMapper.selectByExample(oExample);
                if (null != orderEvaluateDOs && orderEvaluateDOs.size() > 0) {
                    ratedTradeIds.add(trade.getTradeId());
                }
            }

            resultMap.put("trade", tradeViewDO);
        } else {
            logger.error("您的订单已关闭 或者 该订单不是您的订单...... tradeId:" + tradeId + ",profileId:" + profileId + ",sessionId:" + session.getId());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.TRADE_NOT_FOUND.getCode());
            welinkVO.setMsg(BizErrorEnum.TRADE_NOT_FOUND.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        //如果未付款，构建付款参数
        if (Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId() == tradeStatus && !(null != request.getAttribute("isH5") && (boolean) request.getAttribute("isH5"))) {
            //支付相关参数
        	/* ShopDO shop = shopService.fetchShopByShopId(shopId);
            if (null == shop) {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getCode());
                welinkVO.setMsg(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getMsg());
                return JSON.toJSONString(welinkVO);
            }*/
            
            if(trade.getType().equals(Constants.TradeType.crowdfund_type.getTradeTypeId())){
            	Date nowDate = new Date();
            	//MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(trade.getCrowdfundId());
            	List<Byte> statusList = new ArrayList<Byte>();
            	statusList.add(Constants.CrowdfundStatus.NORMAL.getStatusId());
            	statusList.add(Constants.CrowdfundStatus.SUCCESS.getStatusId());
            	MikuCrowdfundDOExample mikuCrowdfundDOExample = new MikuCrowdfundDOExample();
    			Criteria createCriteria = mikuCrowdfundDOExample.createCriteria();
    			createCriteria.andIdEqualTo(trade.getCrowdfundId());
				createCriteria.andStatusIn(statusList);
         		//createCriteria.andStartTimeLessThan(nowDate).andEndTimeGreaterThan(nowDate);
         		if(mikuCrowdfundDOMapper.countByExample(mikuCrowdfundDOExample) < 1){
         			welinkVO.setStatus(0);
                    welinkVO.setMsg("亲~众筹已结束不能进行付款啦~");
                    return JSON.toJSONString(welinkVO);
         		}
            }

            BigDecimal t = new BigDecimal(totalPrice);
            String priceStr = t.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();
            paraMap.put("total_fee", priceStr);
            paraMap.put("partner", AlipayConfig.partner);
            //paraMap.put("seller_id", shop.getAlipayAccount());
            paraMap.put("seller_id", AlipayConfig.alipayAccount);
            paraMap.put("out_trade_no", String.valueOf(tradeId));
            paraMap.put("subject", title);
            paraMap.put("body", body);
            //paraMap.put("notify_url", AlipayConfig.Notify_URL);
            if (env.isProd()) {
                paraMap.put("notify_url", URLEncoder.encode(AlipayConfig.Notify_URL));
            } else {
                paraMap.put("notify_url", URLEncoder.encode(AlipayConfig.APP_DAILY_Notify_URL));
            }
            paraMap.put("payment_type", "1");
            paraMap.put("_input_charset", AlipayConfig.input_charset);
            paraMap.put("it_b_pay", AlipayConfig.OUT_OF_DATE_TIME);
            paraMap.put("service", AlipayConfig.ALIPAY_PAY_INTERFACE);
            paraMap.put("sign_date", String.valueOf(new Date().getTime()));
            String values = AlipayCore.createLinkStringWhitInclude(paraMap);

            String sing = RSA.sign(values, AlipayConfig.input_charset);
            StringUtil stringUtil = new StringUtil();
            resultMap.put("order_info", stringUtil.base64(values));
            resultMap.put("signStr", sing);
        }

        welinkVO.setResult(resultMap);
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        return JSON.toJSONString(welinkVO);
    }

    //setters
    public void setOrderMapper(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    public void setAppointmentService(AppointmentTradeService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public static void main(String[] args) {
        System.out.println(StringUtil.escapeJso1n("hello你好"));
    }
}
