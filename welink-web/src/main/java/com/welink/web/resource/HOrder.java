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

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemCanBuy;
import com.welink.biz.common.model.ItemJson;
import com.welink.biz.common.model.OrderViewDO;
import com.welink.biz.common.model.TradeViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.pay.AlipayConfig;
import com.welink.biz.common.pay.AlipayCore;
import com.welink.biz.common.pay.RSA;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.AddressService;
import com.welink.biz.service.CartService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.StringUtil;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.util.UserUtils;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.BuyItemResultCode;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.ConsigneeAddrDO;
import com.welink.commons.domain.ObjectTaggedDO;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.ShopDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.domain.UserInteractionRecordsDOExample;
import com.welink.commons.events.ProfitEvent;
import com.welink.commons.persistence.ConsigneeAddrDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.commons.tacker.EventTracker;
import com.welink.commons.utils.ApiXUtils;
import com.welink.commons.utils.IdcardUtils;
import com.welink.commons.vo.ApixIdCardMsg;
import com.welink.promotion.PromotionType;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 14-12-5.
 */
@RestController
public class HOrder {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(OrderAction.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private AddressService addressService;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private CartService cartService;

    @Resource
    private ItemService itemService;

    @Resource
    private ShopService shopService;
    
    @Resource
    private ConsigneeAddrDOMapper consigneeAddrDOMapper;
    
    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;
    
    @Resource
	private MemcachedClient memcachedClient; 
    
    @Resource
    private AsyncEventBus asyncEventBus;

    /**
     * 
     * execute:(这里用一句话描述这个方法的作用). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param orderType orderType	下单类型(1=正常下单，8=抽奖下单)默认1
     * @return
     * @throws Exception
     */
    @NeedProfile
    @RequestMapping(value = {"/api/h/1.0/hOrder.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="orderType", required = false, defaultValue="1") Byte orderType,
    		@RequestParam(value="idCard", required = false) String idCard,
    		@RequestParam(value="scratchCardId", required = false, defaultValue="-1") Long scratchCardId,
    		@RequestParam(value="mineScBoxId", required = false, defaultValue="-1") Long mineScBoxId,
    		@RequestParam(value="crowdfundDetailId", required = false, defaultValue="-1") Long crowdfundDetailId) throws Exception {
    	String pUserIdStr = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == pUserIdStr || !StringUtils.isNumeric(pUserIdStr) || StringUtils.isBlank(pUserIdStr)){
        	pUserIdStr = "0";
        }
        Long pUserId = Long.valueOf(pUserIdStr);
        byte payType = ParameterUtil.getParameterAsByteForSpringMVC(request, "pType");
        String message = ParameterUtil.getParameter(request, "msg");
        String items = ParameterUtil.getParameter(request, "items");
        String from = ParameterUtil.getParameter(request, "from");
        long point = ParameterUtil.getParameterAslongForSpringMVC(request, "point", 0);
        long couponId = ParameterUtil.getParameterAslongForSpringMVC(request, "couponId", 0);
        long shippingId = ParameterUtil.getParameterAslongForSpringMVC(request, "spId", 0);//用户自提的时候传自提点id,用户区分是否自提，非自提(配送)时不传参数，直接获取默认收货地址
        Date appointDeliveryTime = ParameterUtil.getParameterAsDateTimeFromLongForSpringMVC(request, BizConstants.APPOINTMENT_DELIVERY_TIME);
        byte shippingType = -1;//1为自提 -1为非自提
        Map<Long, Long> itemCounts = new HashMap<>();
        if (shippingId > 0) {
            shippingType = BizConstants.SELF_PICK_SHIPPING_TYPE;
        }
        WelinkVO welinkVO = new WelinkVO();
        
        if (shippingType == 1) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SELF_PICK_NOT_SUPPORTED.getCode());
            welinkVO.setMsg(BizErrorEnum.SELF_PICK_NOT_SUPPORTED.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        if(StringUtils.isNotBlank(idCard)){
        	//验证身份证是否合法
        	if(!IdcardUtils.validateCard(idCard)){
        		welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.ID_CARD_NOT_SAME.getMsg());
                welinkVO.setCode(BizErrorEnum.ID_CARD_NOT_SAME.getCode());
                return JSON.toJSONString(welinkVO);
        	}
        }
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();

        boolean fromCart = false;
        if (StringUtils.isNotBlank(from) && StringUtils.equalsIgnoreCase(from, "cart")) {
            fromCart = true;
        }
        Map resultMap = new HashMap();
        long profileId = -1;
        long itemId = -1l;
        String mobile = "";
        long tradeId = -1;
        long sellerId = -1;
        long shopId = -1;
        long totalPrice = 0;
        long cachedCommunityId = -1l;
        profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
        EventTracker.track(mobile, "order", "order-action", "order-pre", 1L);
        //fetch user info via profile id
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExample);
        //shopId = (long) session.getAttribute(BizConstants.SHOP_ID);
        shopId = -1L;
        cachedCommunityId = shopId;
        shopId = shopService.fetchIdByShopId(shopId);
        log.info("根据shop_id获取id:" + shopId);
        long communityId = -1l;
        long logisticsId = -1l;
        if (null != profiles && profiles.size() > 0) {
            ConsigneeAddrDO consigneeAddrDO = addressService.fetchDefConsignee(profileId);
            if(null == consigneeAddrDO){
            	welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getMsg());
                welinkVO.setCode(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getCode());
                return JSON.toJSONString(welinkVO);
            }
            if(StringUtils.isNotBlank(idCard)){
            	//验证身份证是否合法
            	if(!IdcardUtils.validateCard(idCard)){
            		welinkVO.setStatus(0);
                    welinkVO.setMsg(BizErrorEnum.ID_CARD_NOT_FOUND.getMsg());
                    welinkVO.setCode(BizErrorEnum.ID_CARD_NOT_FOUND.getCode());
                    return JSON.toJSONString(welinkVO);
            	}
            	if(!idCard.equals(consigneeAddrDO.getIdCard())){
            		//验证身份证的是否真实性
            		ApixIdCardMsg apixIdCardMsg = ApiXUtils.requestGetApixIdCardMsg(consigneeAddrDO.getReceiverName(), idCard.trim());
                	if(null != apixIdCardMsg && !apixIdCardMsg.getCode().equals(0)){
                		welinkVO.setStatus(0);
                        welinkVO.setMsg(apixIdCardMsg.getMsg());
                        welinkVO.setCode(-999);
                        return JSON.toJSONString(welinkVO);
                	}else if(null == apixIdCardMsg){
                		welinkVO.setStatus(0);
                        welinkVO.setMsg(BizErrorEnum.ID_CARD_NOT_FOUND.getMsg());
                        welinkVO.setCode(BizErrorEnum.ID_CARD_NOT_FOUND.getCode());
                        return JSON.toJSONString(welinkVO);
                	}
                	consigneeAddrDO.setIdCard(idCard);
                	//给默认地址加身份证
                	if(consigneeAddrDOMapper.updateByPrimaryKeySelective(consigneeAddrDO) < 1){
                		welinkVO.setStatus(0);
                        welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
                        welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                        return JSON.toJSONString(welinkVO);
                	}
            	}
            }
            //用户自提
            /*if (shippingId > 0) {
                communityId = shippingId;
                if (consigneeAddrDO != null) {
                    logisticsId = addressService.addLogistics(consigneeAddrDO, shippingId, (byte)0);
                    if (logisticsId < 0) {
                        welinkVO.setStatus(0);
                        welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
                        welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
                        Profiler.release();
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
                        welinkVO.setStatus(0);
                        welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
                        welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
                        Profiler.release();
                        return JSON.toJSONString(welinkVO);
                    }
                }
            } else
                //配送
                if (consigneeAddrDO != null) {
                    communityId = consigneeAddrDO.getCommunityId();
                    if (Long.compare(communityId, cachedCommunityId) != 0) {
                        welinkVO.setStatus(0);
                        welinkVO.setCode(BizErrorEnum.COMMUNITY_NOT_MATCHING.getCode());
                        welinkVO.setMsg(BizErrorEnum.COMMUNITY_NOT_MATCHING.getMsg());
                        return JSON.toJSONString(welinkVO);
                    }
                    logisticsId = addressService.addLogistics(consigneeAddrDO, shippingId, (byte)0);
                    if (logisticsId < 0) {
                        welinkVO.setStatus(0);
                        welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
                        welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
                        Profiler.release();
                        return JSON.toJSONString(welinkVO);
                    }
                } else {
                    welinkVO.setStatus(0);
                    welinkVO.setMsg(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getMsg());
                    welinkVO.setCode(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getCode());
                    return JSON.toJSONString(welinkVO);
                }*/
        } else {
            welinkVO.setStatus(0);
            session.setAttribute(BizConstants.PROFILE_ID, -1l);
            try {
                currentUser.logout();
            } catch (Exception e) {
                log.error("logout failed");
            }
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }

        BaseResult<Trade> tradeResult = null;
        List<ImmutablePair<Long, Integer>> itemIdNumPairList = new ArrayList<ImmutablePair<Long, Integer>>();
        List<ItemJson> itemList = new ArrayList<ItemJson>();
        List<Long> itemIds = Lists.newArrayList();

        com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(items);
        for (int i = 0; i < array.size(); i++) {
            com.alibaba.fastjson.JSONObject jobj = (com.alibaba.fastjson.JSONObject) array.get(i);
            ItemJson item = JSON.toJavaObject(jobj, ItemJson.class);
            itemList.add(item);
            itemId = item.getItem_id();
            itemIds.add(item.getItem_id());
        }

        if (itemList.size() < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
        
        if(orderType.equals(Constants.TradeType.crowdfund_type.getTradeTypeId())){	//众筹订单
        	if(crowdfundDetailId < 0){	//
        		welinkVO.setStatus(0);
	            welinkVO.setMsg("亲~众筹不能为空");
	            return JSON.toJSONString(welinkVO);
        	}
        }
        
        if(orderType.equals(Constants.TradeType.crowdfund_type.getTradeTypeId()) && itemList.size() > 1){	//众筹订单
        	welinkVO.setStatus(0);
            welinkVO.setMsg("亲~众筹只能购买单种商品");
            return JSON.toJSONString(welinkVO);
        }
        
        if(orderType.equals(Constants.TradeType.scratch_card.getTradeTypeId()) && itemList.size() > 1){	//刮刮卡订单
        	welinkVO.setStatus(0);
            welinkVO.setMsg("亲~刮刮卡只能领取单种商品~");
            return JSON.toJSONString(welinkVO);
        }
        
        if(orderType.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//定制订单
        	if((null == mineScBoxId || mineScBoxId < 1)){
        		welinkVO.setStatus(0);
        		welinkVO.setMsg("亲~您未选中你需要下单的盒子~");
        		return JSON.toJSONString(welinkVO);
        	}
        	if(items.length() != 1){
        		welinkVO.setStatus(0);
        		welinkVO.setMsg("亲~定制产品只能下一个盒子~");
        		return JSON.toJSONString(welinkVO);
        	}
        }
        
        Date nowDate = new Date();
        ObjectTaggedDO panicObjectTaggedDO = null;
        for(Long itemIdChk : itemIds){
        	panicObjectTaggedDO = itemService.fetchTagObjectsViaItemId(itemIdChk, BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
        	if(null != panicObjectTaggedDO){
        		if(null == panicObjectTaggedDO.getStartTime() || panicObjectTaggedDO.getStartTime().getTime() > nowDate.getTime()){
        			welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                    //welinkVO.setMsg("啊哦~商品抢购未开始");
        			welinkVO.setCode(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_NOT_START.getCode());
        			welinkVO.setMsg(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_NOT_START.getMessage());
                    return JSON.toJSONString(welinkVO);
                }
                if(null == panicObjectTaggedDO.getEndTime() || panicObjectTaggedDO.getEndTime().getTime() < nowDate.getTime()){
                	welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                    //welinkVO.setMsg("啊哦~商品抢购已结束");
                	welinkVO.setCode(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_IS_END.getCode());
        			welinkVO.setMsg(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_IS_END.getMessage());
                    return JSON.toJSONString(welinkVO);
                }
        	}
        }

        for (ItemJson item : itemList) {
            long id = item.getItem_id();
            int quantity;
            if(orderType.equals(Constants.TradeType.lotteryDraw_type.getTradeTypeId()) ||
            		orderType.equals(Constants.TradeType.scratch_card.getTradeTypeId()) || 
            		orderType.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//抽奖订单或刮刮卡订单
            	quantity = 1;
            }else{		//正常订单
            	quantity = item.getNum();
            }
            itemIdNumPairList.add(ImmutablePair.of(id, quantity));
            itemCounts.put(item.getItem_id(), (long) quantity);
        }

        if (itemId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.CAN_NOT_FOUND_ITEM.getMsg());
            welinkVO.setCode(BizErrorEnum.CAN_NOT_FOUND_ITEM.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }

        if(orderType.equals(Constants.TradeType.lotteryDraw_type.getTradeTypeId())){	//抽奖商品检查
        	UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        	userInteractionRecordsDOExample.createCriteria().andUserIdEqualTo(profileId).andTypeEqualTo(PromotionType.ITEM_LOTTERY_DRAW.getCode())
        		.andValueEqualTo(0).andDestinationEqualTo(itemIds.get(0).toString())
        		.andDateCreatedGreaterThan(TimeUtils.addDay(new Date(), -7));	//value(0=未领取；1=已领取)
        	//查询中奖记录
        	List<UserInteractionRecordsDO> itemLotteryDrawList = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
        	if(itemLotteryDrawList.isEmpty()){
        		welinkVO.setStatus(0);
	            welinkVO.setMsg(BuyItemResultCode.NOT_LOTTERY_DRAW_REWARD.getMessage());
	            welinkVO.setCode(BuyItemResultCode.NOT_LOTTERY_DRAW_REWARD.getCode());
	            welinkVO.setResult(resultMap);
	            return JSON.toJSONString(welinkVO);
        	}
        }else{	//正常订单进行限购检查
	        //检查限购数
	        List<ItemCanBuy> outItemCanBuyes = Lists.newArrayList();
	        List<ItemCanBuy> itemCanBuys = itemService.fetchOutLimitItems(itemIds, profileId, false);
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
        }
        com.welink.commons.domain.Item item = itemMapper.selectByPrimaryKey(itemId);
        if (null != item) {
            sellerId = item.getSellerId();
            shopId = item.getShopId();
        }
        //自提不保存配送时间
        if (shippingType == BizConstants.SELF_PICK_SHIPPING_TYPE) {
//            point = 0;
//            couponId = 0;
            appointDeliveryTime = null;
        }
        if (shippingType == 1) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SELF_PICK_NOT_SUPPORTED.getCode());
            welinkVO.setMsg(BizErrorEnum.SELF_PICK_NOT_SUPPORTED.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        Long objectId = null;
        if(orderType.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//定制订单
        	objectId = mineScBoxId;
        }
        Profiler.enter("create trade transaction userId:" + profileId + " create trade");
        if(orderType.equals(Constants.TradeType.lotteryDraw_type.getTradeTypeId())){	//抽奖订单
        	tradeResult = appointmentService.createNewLotteryDrawTrade(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
        			Constants.TradeFrom.H5.getTypeId(), payType, logisticsId, shippingType, appointDeliveryTime);
        }else if(orderType.equals(Constants.TradeType.crowdfund_type.getTradeTypeId())){	//众筹订单
        	tradeResult = appointmentService.createNewAppointment(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
        			Constants.TradeFrom.H5.getTypeId(), payType, logisticsId, shippingType, 0L, -1L, appointDeliveryTime, orderType, objectId, crowdfundDetailId, scratchCardId, pUserId);
        }else{	//正常订单
        	tradeResult = appointmentService.createNewAppointment(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
                Constants.TradeFrom.H5.getTypeId(), payType, logisticsId, shippingType, point, couponId, appointDeliveryTime, orderType, objectId, crowdfundDetailId, scratchCardId, pUserId);
        }
        Profiler.release();
        //query new trade
        if (tradeResult.isSuccess() && tradeResult.getResult() != null) {
            //记录购买记录
            itemService.recordBuy(itemCounts, profileId, tradeResult.getResult().getTradeId());

            long tid = tradeResult.getResult().getId();
            Trade trade = tradeMapper.selectByPrimaryKey(tid);
            if (null != trade) {
                totalPrice = trade.getTotalFee();
                if (fromCart) {
                    //清除购物车--购物车已支持部分购买，所以这里是更新购物车数据
                    Map<Long, Long> toUpdateItemCounts = new HashMap<>();
                    for (Long id : itemCounts.keySet()) {
                        toUpdateItemCounts.put(id, 0 - itemCounts.get(id));
                    }
                    cartService.updateItem2Cart(profileId, toUpdateItemCounts);
                }
                tradeId = trade.getTradeId();
                resultMap.put("tradeId", String.valueOf(tradeId));
                resultMap.put("pType", payType);
                resultMap.put("tradeStatus", trade.getStatus());
                resultMap.put("tradeType", trade.getType());
            } else {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.CREATE_TRADE_FAILED.getMsg());
                welinkVO.setCode(BizErrorEnum.CREATE_TRADE_FAILED.getCode());
                return JSON.toJSONString(welinkVO);
            }
        } else {
            log.error("create trade failed. itemId:" + itemId + ",profileId:" + profileId + ",sessionId:" + session.getId());
            welinkVO.setStatus(0);
            welinkVO.setMsg(tradeResult.getMessage());
            welinkVO.setCode(tradeResult.getCode());
            resultMap.put("failItems", tradeResult.getFailItems());
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }

        //货到付款处理 0元付款
        if (((Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0 || totalPrice == 0) && tradeId > 0)) {
            log.info("cod pay type - trade created success......" + ",sessionId:" + session.getId().toString());
            welinkVO.setStatus(1);
            if (totalPrice == 0) {
                resultMap.put("pType", Constants.PayType.ONLINE_ZERO_PAY.getPayTypeId());
            }
            if (tradeId > 0) {	//交易分润
            	asyncEventBus.post(new ProfitEvent(tradeId, 1));	//通过事件总线 交易分润 //1=加分润；2=减分润
            	//profitService.profitByTradeId(tradeId);
    		}
            //清除购物车--购物车已支持部分购买，所以这里是更新购物车数据
            if (StringUtils.equals(from, "cart")) {
                Map<Long, Long> toUpdateItemCounts = new HashMap<>();
                for (Long id : itemCounts.keySet()) {
                    toUpdateItemCounts.put(id, 0 - itemCounts.get(id));
                }
                cartService.updateItem2Cart(profileId, toUpdateItemCounts);
            }
            welinkVO.setResult(resultMap);
            //清除购物车--购物车已支持部分购买，所以这里是更新购物车数据
            /*
            if (fromCart) {
                Map<Long, Long> toUpdateItemCounts = new HashMap<>();
                for (Long id : itemCounts.keySet()) {
                    toUpdateItemCounts.put(id, 0 - itemCounts.get(id));
                }
                cartService.updateItem2Cart(profileId, toUpdateItemCounts);
            }
            */
            //减库存 - 付款后减库存
//            itemService.updateStock(tradeResult.getResult());
            return JSON.toJSONString(welinkVO);
        }

        //创建订单成功
        if (tradeId > 0) {
            welinkVO.setStatus(1);
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }
        //创建订单失败
        else {
            welinkVO.setStatus(0);
            welinkVO.setCode(tradeResult.getCode());
            welinkVO.setMsg(tradeResult.getMessage());
            return JSON.toJSONString(welinkVO);
        } 
    }
    
    /**
     * 
     * hOrderDz:(定制下单). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param detectReportId	测肤报表id
     * @return
     * @throws Exception
     * @since JDK 1.6
     */
    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/hOrderDz.json"}, produces = "application/json;charset=utf-8")
    public String hOrderDz(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="detectReportId", required = true) Long detectReportId) throws Exception {
        String from = request.getParameter("from");
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        Date nowDate = new Date();
        
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
        //非货到付款 获取配送时间
        /*if (shippingId < 1) {//非自提
            if (StringUtils.isBlank(ParameterUtil.getParameter(request, BizConstants.APPOINTMENT_DELIVERY_TIME))) {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                return JSON.toJSONString(welinkVO);
            }
            appointDeliveryTime = ParameterUtil.getParameterAsDateTimeFromLongForSpringMVC(request, BizConstants.APPOINTMENT_DELIVERY_TIME);
        }*/
        long point = ParameterUtil.getParameterAslongForSpringMVC(request, "point", 0);
        long couponId = ParameterUtil.getParameterAslongForSpringMVC(request, "couponId", 0);
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
        //默认使用支付宝支付
        if (payType < 0) {
            payType = Constants.PayType.ONLINE_ALIPAY.getPayTypeId();
        }
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }

        //货到付款不支持使用优惠
        if (Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0 && (point > 0 || couponId > 0)) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.COD_NOT_SUPPORT_PROMOTION.getCode());
            welinkVO.setMsg(BizErrorEnum.COD_NOT_SUPPORT_PROMOTION.getMsg());
            return JSON.toJSONString(welinkVO);
        }

        EventTracker.track(mobile, "order", "order-action", "order-pre", 1L);
        String message = ParameterUtil.getParameter(request, "msg");
        //fetch user info via profile id
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExample);
        long consigneeId = -1l;
        long communityId = -1l;
        long logisticsId = -1l;
        if (null != profiles && profiles.size() > 0) {
            ConsigneeAddrDO consigneeAddrDO = addressService.fetchDefConsignee(profileId);
            if(null == consigneeAddrDO){
            	welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getMsg());
                welinkVO.setCode(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getCode());
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

        Long cachedCommunityId = -1L;

        BaseResult<Trade> tradeResult = null;
        
        if (profileId == -1) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }

        Constants.TradeFrom tradeFrom = UserUtils.differentiateOS(request);
        Profiler.enter("create trade transaction userId:" + profileId + " create trade");
        appointmentService.createNewAppointmentDz(sellerId, shopId, point, couponId, communityId, 
        		profileId, -1L, new Date(), null, message, 
        		tradeFrom.getTypeId(), payType, consigneeId, shippingType, appointDeliveryTime, detectReportId);
        Profiler.release();
        Profiler.release();
        //query new trade
        if (tradeResult.isSuccess() && tradeResult.getResult() != null) {
            long tid = tradeResult.getResult().getId();
            Trade trade = tradeMapper.selectByPrimaryKey(tid);
            if (null != trade) {
                totalPrice = trade.getTotalFee();
                tradeId = trade.getTradeId();
                resultMap.put("tradeId", String.valueOf(tradeId));
                resultMap.put("pType", payType);
                resultMap.put("tradeStatus", trade.getStatus());
                resultMap.put("tradeType", trade.getType());
            } else {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.CREATE_TRADE_FAILED.getMsg());
                welinkVO.setCode(BizErrorEnum.CREATE_TRADE_FAILED.getCode());
                return JSON.toJSONString(welinkVO);
            }
        } else {
            welinkVO.setStatus(0);
            welinkVO.setMsg(tradeResult.getMessage());
            welinkVO.setCode(tradeResult.getCode());
            resultMap.put("failItems", tradeResult.getFailItems());
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }

        //货到付款处理 0元付款
        if (((Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0 || totalPrice == 0) && tradeId > 0)) {
            log.info("cod pay type - trade created success......" + ",sessionId:" + session.getId().toString());
            welinkVO.setStatus(1);
            if (totalPrice == 0) {
                resultMap.put("pType", Constants.PayType.ONLINE_ZERO_PAY.getPayTypeId());
            }
            if (tradeId > 0) {	//交易分润
            	asyncEventBus.post(new ProfitEvent(tradeId, 1));	//通过事件总线 交易分润 //1=加分润；2=减分润
    		}
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }

        //创建订单成功
        if (tradeId > 0) {
            welinkVO.setStatus(1);
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }
        //创建订单失败
        else {
            welinkVO.setStatus(0);
            welinkVO.setCode(tradeResult.getCode());
            welinkVO.setMsg(tradeResult.getMessage());
            return JSON.toJSONString(welinkVO);
        }
    }
    
}
