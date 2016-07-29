/**
 * Project Name:welink-web
 * File Name:LotteryDrawRewardControler.java
 * Package Name:com.welink.web.resource
 * Date:2015年12月22日上午11:07:59
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

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
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemJson;
import com.welink.biz.common.model.OrderViewDO;
import com.welink.biz.common.model.TradeViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.AddressService;
import com.welink.biz.service.CartService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.LotteryDrawInteractionService;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.UserUtils;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.ConsigneeAddrDO;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.domain.UserInteractionRecordsDOExample;
import com.welink.commons.persistence.CommunityDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.commons.vo.LotteryDrawRewardVO;
import com.welink.commons.vo.RewardItemVO;
import com.welink.promotion.PromotionType;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;

/**
 * ClassName:LotteryDrawController <br/>
 * Function: 抽奖接口. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月22日 上午11:07:59 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class LotteryDrawController {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(LotteryDrawController.class);
	
	@Resource
	private LotteryDrawInteractionService lotteryDrawInteractionService;
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;
	
	@Resource
    private AppointmentTradeService appointmentService;
	
	@Resource
    private OrderMapper orderMapper;
	
	@Resource
    private ItemMapper itemMapper;

    @Resource
    private ShopService shopService;

    @Resource
    private AddressService addressService;

    @Resource
    private CommunityDOMapper communityDOMapper;

    @Resource
    private CartService cartService;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private ItemService itemService;
	
	/**
	 * 
	 * execute:(抽奖接口). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@NeedProfile
    //@RequestMapping(value = {"/api/m/1.0/lotteryDrawMikAtctive.json", "/api/h/1.0/lotteryDrawMikAtctive.json"}, produces = "application/json;charset=utf-8")
	@RequestMapping(value = {"/api/m/1.0/lotteryDraw.json", "/api/h/1.0/lotteryDraw.json"}, produces = "application/json;charset=utf-8")
    public String lotteryDraw(HttpServletRequest request, HttpServletResponse response) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
        Map resultMap = new HashMap();
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        long profileId = -1;
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        LotteryDrawRewardVO lotteryDrawRewardVO = lotteryDrawInteractionService.interactive(profileId);
        /*LotteryDrawRewardVO lotteryDrawRewardVO = null;
        String unesTest = ParameterUtil.getParameter(request, "unesTest");
        if(Utils.lucky("0.8") && null != unesTest && unesTest.equals("unesTest")){
        	lotteryDrawRewardVO = new LotteryDrawRewardVO();
        	Random r = new Random();
        	Integer type = r.nextInt(3)+1;
        	lotteryDrawRewardVO.setType(type);
        	//lotteryDrawRewardVO.setType(r.nextInt(3)+1);
        	if(type.equals(1)){
        		if(Utils.lucky("0.5")){
        			lotteryDrawRewardVO.setIndex(1001);
        		}else{
        			lotteryDrawRewardVO.setIndex(1002);
        		}
        	}else if(type.equals(2)){
        		lotteryDrawRewardVO.setIndex(2001);
        	}else{
        		if(Utils.lucky("0.5")){
        			lotteryDrawRewardVO.setIndex(3001);
            		lotteryDrawRewardVO.setRewardId(100L);
        		}else{
        			lotteryDrawRewardVO.setIndex(3002);
            		lotteryDrawRewardVO.setRewardId(200L);
        		}
        		
        	}
        	
        }else{
        	return null;
        }*/
        
        
		if(null == lotteryDrawRewardVO){
			lotteryDrawRewardVO = new LotteryDrawRewardVO();
			lotteryDrawRewardVO.setType(-1);
			lotteryDrawRewardVO.setIndex(-1);
		}
		resultMap.put("lotteryDrawRewardVO", lotteryDrawRewardVO);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * lotteryDrawTimes:(获取用户可抽奖次数). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 * @since JDK 1.6
	 */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/lotteryDrawTimes.json", "/api/h/1.0/lotteryDrawTimes.json"}, produces = "application/json;charset=utf-8")
    public String lotteryDrawTimes(HttpServletRequest request, HttpServletResponse response) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
        Map resultMap = new HashMap();
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        long profileId = -1;
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        //抽奖次数
        int tatol = lotteryDrawInteractionService.countUserInteractionRecordsDO(profileId, PromotionType.CHANCE_LOTTERY_DRAW.getCode(), false);
        //已抽奖次数
        int usetatol = lotteryDrawInteractionService.countUserInteractionRecordsDO(profileId, PromotionType.USE_CHANCE_LOTTERY_DRAW.getCode(), false);
        int times = tatol-usetatol;	//剩余抽奖次数
        if((times) < 1){	//没有抽奖机会
        	times = 0;
        }
        resultMap.put("times", times);	//剩余抽奖次数
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/confirmLotteryDraw.json", "/api/h/1.0/confirmLotteryDraw.json"}, produces = "application/json;charset=utf-8")
    public String confirmLotteryDraw(HttpServletRequest request, HttpServletResponse response) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
		long profileId = -1;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        Map resultMap = new HashMap();
        try {
            if (session == null || !sessionObject(session, "profileId")) {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                return JSON.toJSONString(welinkVO);
            }
            profileId = (long) session.getAttribute("profileId");
        } catch (Exception e) {
            log.error("fetch paras from session failed . exp:" + e.getMessage() + ",sessionId:" + session.getId());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }

        resultMap.put("consigned", "0");
        resultMap.put("postFeeStep", Constants.POST_FEE_STEP);

        String items = ParameterUtil.getParameter(request, "items");
        if (StringUtils.isBlank(items)) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            return JSON.toJSONString(welinkVO);
        }
        com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(items);
        if (array.size() < 1) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            return JSON.toJSONString(welinkVO);
        }
        List<ItemJson> itemList = new ArrayList<ItemJson>();
        for (int i = 0; i < array.size(); i++) {
            com.alibaba.fastjson.JSONObject jobj = (com.alibaba.fastjson.JSONObject) array.get(i);
            ItemJson item = JSON.toJavaObject(jobj, ItemJson.class);
            itemList.add(item);
        }
        Map<Long, Integer> itemNumMap = new HashMap<Long, Integer>();
        Map<String, com.welink.commons.domain.Item> itemMap = new HashMap<>();
        if (itemList.size() > 0 && itemList.get(0).getItem_id() > 0) {
        	Item item = itemMapper.selectByPrimaryKey(itemList.get(0).getItem_id());
        	if(null != item ){
	    		UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
	    		userInteractionRecordsDOExample.createCriteria().andUserIdEqualTo(profileId)
	    		.andValueEqualTo(0).andDestinationEqualTo(String.valueOf(itemList.get(0).getItem_id()));
	    		int countUserInteractionRecords = userInteractionRecordsDOMapper.countByExample(userInteractionRecordsDOExample);
	    		if(countUserInteractionRecords > 0){
	    			//2. 收货地址
	    			ConsigneeAddrDO consigneeAddrDO = addressService.fetchDefConsignee(profileId);
	                if (null != consigneeAddrDO) {
	                    resultMap.put("consignee_name", consigneeAddrDO.getReceiverName());
	                    resultMap.put("mobile", consigneeAddrDO.getReceiverMobile());
	                    resultMap.put("addr", consigneeAddrDO.getReceiver_state()+ consigneeAddrDO.getReceiverCity() + consigneeAddrDO.getReceiverDistrict() + "" + consigneeAddrDO.getReceiverAddress());
	                    resultMap.put("community_id", consigneeAddrDO.getCommunityId());
	                    resultMap.put("consigned", "1");
	                }
	                resultMap.put("post_fee", Constants.POST_FEE);
	                resultMap.put("totalFee", Constants.POST_FEE);
	                List<BuyItemViewDO> buyItemViewDOs = new ArrayList<BuyItemViewDO>();
	                BuyItemViewDO buyItemViewDO = new BuyItemViewDO();
	                buyItemViewDO.setNum(1);
	                buyItemViewDO.setPrice(item.getPrice());
	                buyItemViewDO.setItemId(item.getId());
	                buyItemViewDO.setPics(item.getPicUrls());
	                buyItemViewDO.setTitle(item.getTitle());
	                buyItemViewDOs.add(buyItemViewDO);
	                resultMap.put("items", buyItemViewDOs);
	                
	                List<Byte> payTypes = new ArrayList<>();
	                //先去掉货到付款
//	                payTypes.add(Constants.PayType.OFF_LINE.getPayTypeId());
	                payTypes.add(Constants.PayType.ONLINE_ALIPAY.getPayTypeId());
	                payTypes.add(Constants.PayType.ONLINE_WXPAY.getPayTypeId());
	                resultMap.put("payTypes", payTypes);
	                welinkVO.setStatus(1);
	                welinkVO.setResult(resultMap);
	                return JSON.toJSONString(welinkVO);
	    		}else{
	    			welinkVO.setStatus(0);
	                welinkVO.setMsg("该奖品已领取或超出领取日期");
	                return JSON.toJSONString(welinkVO);
	    		}
        	}
        }
    	welinkVO.setStatus(0);
    	welinkVO.setMsg(BizErrorEnum.CAN_NOT_FOUND_ITEM.getMsg());
        welinkVO.setCode(BizErrorEnum.CAN_NOT_FOUND_ITEM.getCode());
        return JSON.toJSONString(welinkVO);
	}
	
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/lotteryDrawOrder.json", "/api/h/1.0/lotteryDrawOrder.json"}, produces = "application/json;charset=utf-8")
    public String lotteryDrawOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String items = ParameterUtil.getParameter(request, "items");
        Map<Long, Long> itemCounts = new HashMap<>();
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        byte shippingType = -1;
        Date appointDeliveryTime = null;
        long shippingId = ParameterUtil.getParameterAslongForSpringMVC(request, "spId", 0);		
        if (shippingId > 0) {
            shippingType = 1;
        }
        if (shippingType == 1) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SELF_PICK_NOT_SUPPORTED.getCode());
            welinkVO.setMsg(BizErrorEnum.SELF_PICK_NOT_SUPPORTED.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        byte payType = ParameterUtil.getParameterAsByteForSpringMVC(request, "pType");
        //默认使用支付宝支付
        if (payType < 0) {
            payType = Constants.PayType.ONLINE_ALIPAY.getPayTypeId();
        }
        Map<String, String> paraMap = new HashMap<>();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        long profileId = -1;
        String mobile = "";
        String title = "";
        String body = "";
        long tradeId = -1;
        long sellerId = -1;
        long shopId = -1;
        long totalPrice = 0;
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        String message = ParameterUtil.getParameter(request, "msg");
        
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExample);
        long consigneeId = -1l;
        long communityId = -1l;
        long logisticsId = -1l;
        if (null != profiles && profiles.size() > 0) {
            ConsigneeAddrDO consigneeAddrDO = addressService.fetchDefConsignee(profileId);
            if (shippingId > 0) {
                communityId = shippingId;
                if (consigneeAddrDO != null) {
                    logisticsId = addressService.addLogistics(consigneeAddrDO, shippingId, (byte)0);
                    if (logisticsId < 0) {
                        welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                        welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
                        welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
                        return JSON.toJSONString(welinkVO);
                    }
                } else {
                    consigneeAddrDO = new ConsigneeAddrDO();
                    consigneeAddrDO.setReceiverMobile(profiles.get(0).getMobile());
                    consigneeAddrDO.setReceiverPhone(profiles.get(0).getMobile());
                    consigneeAddrDO.setUserId(profileId);
                    consigneeAddrDO.setReceiverName(profiles.get(0).getRealName());
                    logisticsId = addressService.addLogistics(consigneeAddrDO, shippingId, (byte)0);
                    if (logisticsId < 0) {
                        welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                        welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
                        welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
                        return JSON.toJSONString(welinkVO);
                    }
                }
            } else if (consigneeAddrDO != null) {
                communityId = consigneeAddrDO.getCommunityId();
                logisticsId = addressService.addLogistics(consigneeAddrDO, shippingId, (byte)0);
                if (logisticsId < 0) {
                    welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                    welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
                    welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
                    return JSON.toJSONString(welinkVO);
                }
            } else {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getCode());
                welinkVO.setMsg(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        } else {
            session.setAttribute(BizConstants.PROFILE_ID, -1l);
            try {
                currentUser.logout();
            } catch (Exception e) {
                log.error("logout failed");
            }
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        
        //校验communityId
        Long cachedCommunityId = (Long) session.getAttribute(BizConstants.SHOP_ID);
        /*log.error("========cached c id :" + cachedCommunityId);
        log.error("======== c id :" + communityId);
        if (Long.compare(communityId, cachedCommunityId) != 0) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.NO_EQUAL_COMMUNITY.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_EQUAL_COMMUNITY.getMsg());
            return JSON.toJSONString(welinkVO);
        }*/
        
        BaseResult<Trade> tradeResult = null;
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
            //int quantity = item.getNum();
            int quantity = 1;
            itemIdNumPairList.add(ImmutablePair.of(id, quantity));
            itemCounts.put(item.getItem_id(), (long) quantity);
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
        }
        if (profileId == -1) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        Constants.TradeFrom tradeFrom = UserUtils.differentiateOS(request);
        tradeResult = appointmentService.createNewLotteryDrawTrade(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
                tradeFrom.getTypeId(), payType, logisticsId, shippingType, appointDeliveryTime);
        boolean tradeCreated = false;
        if (tradeResult.isSuccess() && tradeResult.getResult() != null) {
            //记录活动购买记录
            long tid = tradeResult.getResult().getId();
            Trade trade = tradeMapper.selectByPrimaryKey(tid);
            if (null != trade) {
            	
                //查找站點
                CommunityDO communityDO = communityDOMapper.selectByPrimaryKey(communityId);

                TradeViewDO tradeViewDO = ViewDOCopy.buildTradeViewDO(trade, communityDO);
                //记录购买记录
                itemService.recordBuy(itemCounts, profileId, tradeResult.getResult().getTradeId());

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
                }
                List<OrderViewDO> orderViewDOs = Lists.newArrayList();
                for (Order order : orders) {
                    orderViewDOs.add(ViewDOCopy.buildOrderViewDO(order));
                }
                tradeViewDO.setOrderViewDOs(orderViewDOs);
                resultMap.put("trade", tradeViewDO);
                Profiler.release();
            } else {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.CREATE_TRADE_FAILED.getCode());
                welinkVO.setMsg(BizErrorEnum.CREATE_TRADE_FAILED.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        } else {
            log.error("create trade failed. itemId:" + itemId + ",profileId:" + profileId + ",sessionId:" + session.getId().toString());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(tradeResult.getCode());
            welinkVO.setMsg(tradeResult.getMessage());
            return JSON.toJSONString(welinkVO);
        }
        
        //货到付款处理
        //if ((Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0 || totalPrice == 0) && tradeCreated) {
        log.info("cod pay type - trade created success......" + ",sessionId:" + session.getId().toString());
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        resultMap.put("trade_id", tradeId);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
        
	}
	
	/**
	 * 
	 * itemLotteryDrawList:(获取用户中奖商品). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 * @since JDK 1.6
	 */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/itemLotteryDrawList.json", "/api/h/1.0/itemLotteryDrawList.json"}, produces = "application/json;charset=utf-8")
    public String itemLotteryDrawList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        long profileId = -1;
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.setOrderByClause("id DESC");
        userInteractionRecordsDOExample.createCriteria() //
                .andUserIdEqualTo(profileId) //
                .andTypeEqualTo(PromotionType.ITEM_LOTTERY_DRAW.getCode());

        List<RewardItemVO> rewards = new ArrayList<RewardItemVO>();
        List<UserInteractionRecordsDO> userInteractionRecordsDOs = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
        for(UserInteractionRecordsDO userInteractionRecordsDO : userInteractionRecordsDOs){
        	RewardItemVO rewardItemVO = new RewardItemVO();
        	rewardItemVO.setId(userInteractionRecordsDO.getId());
        	rewardItemVO.setUserId(userInteractionRecordsDO.getUserId());
        	rewardItemVO.setValue(userInteractionRecordsDO.getValue());
        	rewardItemVO.setType(userInteractionRecordsDO.getType());
        	rewardItemVO.setTargetId(userInteractionRecordsDO.getTargetId());
        	rewardItemVO.setDestination(userInteractionRecordsDO.getDestination());
        	rewardItemVO.setDateCreated(userInteractionRecordsDO.getDateCreated());
        	if(null != userInteractionRecordsDO.getDestination()){
        		Item item = itemMapper.selectByPrimaryKey(Long.valueOf(userInteractionRecordsDO.getDestination()));
        		if(null != item){
        			rewardItemVO.setItemTitle(item.getTitle());
        			rewardItemVO.setItemPrice(item.getPrice());
        			rewardItemVO.setPics(item.getPicUrls());
        		}
        	}
        	rewards.add(rewardItemVO);
        }
        
        if(!rewards.isEmpty()){
        	resultMap.put("rewards", rewards);
        }
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	
	public class BuyItemViewDO {
        //要买的个数
        private int num;
        //宝贝图片url
        private String pics;
        //宝贝标题
        private String title;
        //宝贝价格
        private long price;
        //宝贝id
        private long itemId;

        public long getPrice() {
            return price;
        }

        public void setPrice(long price) {
            this.price = price;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public String getPics() {
            return pics;
        }

        public void setPics(String pics) {
            this.pics = pics;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getItemId() {
            return itemId;
        }

        public void setItemId(long itemId) {
            this.itemId = itemId;
        }
    }
	
	private boolean sessionObject(Session session, String key) {
        if (null == session) {
            return false;
        }
        return null != session.getAttribute(key);
    }
	
}

