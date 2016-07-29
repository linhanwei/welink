/**
 * Project Name:welink-web
 * File Name:MikuSalesRecord.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月2日下午7:07:42
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemCanBuy;
import com.welink.biz.common.model.ItemJson;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.pay.AlipayConfig;
import com.welink.biz.common.pay.AlipayCore;
import com.welink.biz.common.pay.RSA;
import com.welink.biz.service.CartService;
import com.welink.biz.service.MikuOneBuyService;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.StringUtil;
import com.welink.biz.util.UserUtils;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.MikuOneBuyDO;
import com.welink.commons.domain.MikuOneBuyDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.ShopDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.persistence.CommunityDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuOneBuyDOMapper;
import com.welink.commons.persistence.MikuUserOneBuyDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;

/**
 * ClassName:MikuSalesRecord <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月2日 下午7:07:42 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class OneBuyOrderAction {
	
	@Resource
	private MikuOneBuyDOMapper mikuOneBuyDOMapper;
	
	@Resource
	private MikuUserOneBuyDOMapper mikuUserOneBuyDOMapper;
	
	@Resource
	private MikuOneBuyService mikuOneBuyService;
	
	@Resource
    private ItemMapper itemMapper;
	
	@Resource
    private OrderMapper orderMapper;
	
	@Resource
    private TradeMapper tradeMapper;

    @Resource
    private ShopService shopService;
    
    @Resource
    private CartService cartService;
    
    @Resource
    private CommunityDOMapper communityDOMapper;
    
    @Resource
    private Env env;
	
	@RequestMapping(value = {"/api/m/1.0/oneBuyOrder.json", "/api/h/1.0/oneBuyOrder.json"}, produces = "application/json;charset=utf-8")
	public String oneBuyOrder(HttpServletRequest request, HttpServletResponse response
    		) throws Exception {
		//@RequestParam(value="isGetpays", required = false) String isGetpays
		
		String from = request.getParameter("from");
		String items = ParameterUtil.getParameter(request, "items");
        Map<Long, Long> itemCounts = new HashMap<>();
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
		
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		byte payType = ParameterUtil.getParameterAsByteForSpringMVC(request, "pType");
		long shippingId = ParameterUtil.getParameterAslongForSpringMVC(request, "spId", 0);
		String message = ParameterUtil.getParameter(request, "msg");
		
		String body = "";
		long tradeId = -1;
        long sellerId = -1;
        long shopId = -1;
        long communityId = -1l;
        long totalPrice = 0;
        String title = "";
        
		//验证下单
		/*MikuOneBuyDOExample mikuOneBuyDOExample = new MikuOneBuyDOExample();
		mikuOneBuyDOExample.createCriteria().andItemIdEqualTo(1L).andStatusEqualTo((byte)1);*/
		
		List<ImmutablePair<Long, Integer>> itemIdNumPairList = new ArrayList<ImmutablePair<Long, Integer>>();

        com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(items);
        List<ItemJson> itemList = new ArrayList<ItemJson>();
        long itemId = -1l;
        List<Long> itemIds = Lists.newArrayList();
        for (int i = 0; i < array.size(); i++) {
            com.alibaba.fastjson.JSONObject jobj = (com.alibaba.fastjson.JSONObject) array.get(i);
            ItemJson item = JSON.toJavaObject(jobj, ItemJson.class);
            itemList.add(item);
            itemId = item.getItem_id();
            itemIds.add(itemId);
        }
        if (itemList.size() < 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        
        for (ItemJson item : itemList) {
            long id = item.getItem_id();
            itemIdNumPairList.add(ImmutablePair.of(id, item.getNum()));
            itemCounts.put(item.getItem_id(), (long) item.getNum());
        }
        if (itemId < 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.CAN_NOT_FOUND_ITEM.getCode());
            welinkVO.setMsg(BizErrorEnum.CAN_NOT_FOUND_ITEM.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        
        com.welink.commons.domain.Item item = itemMapper.selectByPrimaryKey(itemId);
        if (null != item) {
            sellerId = item.getSellerId();
            shopId = item.getShopId();
            if(shopId > 0){
            	CommunityDO communityDO = communityDOMapper.selectByPrimaryKey(shopId);
            	if(null != communityDO){
            		communityId = communityDO.getId();
            	}
            }
        }
		
        //限购检查
        List<ItemCanBuy> outItemCanBuyes = Lists.newArrayList();
        List<ItemCanBuy> itemCanBuys = mikuOneBuyService.fetchOutLimitItems(itemIds, profileId);
        for (Long id : itemCounts.keySet()) {
            for (ItemCanBuy itemCanBuy : itemCanBuys) {
                if (Long.compare(id, itemCanBuy.getItemId()) == 0 && itemCounts.get(id) > itemCanBuy.getCap()) {
                    outItemCanBuyes.add(itemCanBuy);
                }
            }
        }
        if (outItemCanBuyes.size() > 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.ITEM_LIMIT_BUY_COUNTS.getMsg());
            welinkVO.setCode(BizErrorEnum.ITEM_LIMIT_BUY_COUNTS.getCode());
            resultMap.put("outItems", outItemCanBuyes);
            welinkVO.setResult(resultMap);
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
        //获取用户设备信息
        Constants.TradeFrom tradeFrom = UserUtils.differentiateOS(request);
        byte shippingType = -1;
		//下单
        BaseResult<Trade> tradeResult = null;
        tradeResult = mikuOneBuyService.createNewOneBuyTrade(sellerId, shopId, communityId, profileId, 
        		itemIdNumPairList, null, message, tradeFrom.getTypeId(), payType, shippingType);
        
        boolean tradeCreated = false;
        if (tradeResult.isSuccess() && tradeResult.getResult() != null) {
            //记录活动购买记录
            long tid = tradeResult.getResult().getId();
            Trade trade = tradeMapper.selectByPrimaryKey(tid);
            if (null != trade) {
            	tradeCreated = true;
            	tradeId = trade.getTradeId();
            	totalPrice = trade.getTotalFee();
                title = trade.getTitle();
            	
            	List<Order> orders = new ArrayList<Order>();
                Profiler.enter("fetch orders of trade ." + profileId + " orders");
                if (trade.getOrders().length() > 0) {
                    for (String id : trade.getOrders().split(";")) {
                        OrderExample orderExample = new OrderExample();
                        orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
                        List<Order> tmpOrders = new ArrayList<Order>();
                        tmpOrders = orderMapper.selectByExample(orderExample);
                        if (null != tmpOrders && tmpOrders.size() > 0) {
                            orders.add(tmpOrders.get(0));
                            body += tmpOrders.get(0).getTitle() + "-" + tmpOrders.get(0).getId() + "-";
                        }
                    }
                    
                    //清除购物车--购物车已支持部分购买，所以这里是更新购物车数据
	                if (StringUtils.equals(from, "cart")) {
	                    Map<Long, Long> toUpdateItemCounts = new HashMap<>();
	                    for (Long id : itemCounts.keySet()) {
	                        toUpdateItemCounts.put(id, 0 - itemCounts.get(id));
	                    }
	                    cartService.updateItem2Cart(profileId, toUpdateItemCounts);
	                }
                    
                }
            }else {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.CREATE_TRADE_FAILED.getCode());
                welinkVO.setMsg(BizErrorEnum.CREATE_TRADE_FAILED.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        }
        
        //生成支付相关 获取店铺支付宝账户
        ShopDO shop = shopService.fetchShopByShopId(shopId);
        if (null == shop) {
        	welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
        	welinkVO.setCode(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getCode());
        	welinkVO.setMsg(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getMsg());
        	return JSON.toJSONString(welinkVO);
        }
		//支付参数
        Profiler.enter("build param map for alipay ." + profileId + " build param");
        BigDecimal t = new BigDecimal(totalPrice);
        String priceStr = t.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("total_fee", priceStr);
        paraMap.put("partner", AlipayConfig.partner);
        paraMap.put("seller_id", shop.getAlipayAccount());
        paraMap.put("out_trade_no", String.valueOf(tradeId));
        paraMap.put("subject", title);
        paraMap.put("body", body);
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
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        welinkVO.setResult(resultMap);
        
        return JSON.toJSONString(welinkVO);
	}
	
}

