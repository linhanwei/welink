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
import com.welink.biz.service.UsePromotionService;
import com.welink.biz.util.StringUtil;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.util.UserUtils;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.BuyItemResultCode;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
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
import com.welink.commons.persistence.CommunityDOMapper;
import com.welink.commons.persistence.ConsigneeAddrDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.OrderMapper;
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
 * Created by daniel on 14-9-16.
 */
@RestController
public class OrderAction {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(OrderAction.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UsePromotionService usePromotionService;

    @Resource
    private ProfileDOMapper profileDOMapper;

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
    
    @Resource
    private ConsigneeAddrDOMapper consigneeAddrDOMapper;
    
    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;
    
    @Resource
    private AsyncEventBus asyncEventBus;
    
    @Resource
	private MemcachedClient memcachedClient; 
    
    @Resource
    private Env env;

    /**
     * 
     * execute:(这里用一句话描述这个方法的作用). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param scratchCardId 刮刮卡id
     * @param orderType	下单类型(1=正常下单，8=抽奖下单)默认1
     * 	fixed((byte) 1, "一口价"),
        cod((byte) 2, "货到付款"),
        groupon((byte) 3, "万人团"),
        nopaid((byte) 4, "无付款订单"),
        pre_auth_type((byte) 5, "预授权0元购机交易"),
        one_buy_type((byte) 6, "一元购"),
        crowdfund_type((byte) 7, "众筹"),
        lotteryDraw_type((byte) 8, "抽奖"),
        join_agency((byte) 9, "成为代理"),
        scratch_card((byte) 10, "刮刮卡")
        topic_cut((byte) 11, "满减"),
        dz_type((byte) 12, "定制"),
     * @return
     * @throws Exception
     * @since JDK 1.6
     */
    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/order.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="orderType", required = false, defaultValue="1") Byte orderType,
    		@RequestParam(value="idCard", required = false) String idCard,
    		@RequestParam(value="scratchCardId", required = false, defaultValue="-1") Long scratchCardId,
    		@RequestParam(value="crowdfundDetailId", required = false, defaultValue="-1") Long crowdfundDetailId,
    		@RequestParam(value="mineScBoxId", required = false, defaultValue="-1") Long mineScBoxId,
    		@RequestParam(value="pUserId", required = false, defaultValue="0") Long pUserId) throws Exception {
        //get parameters
        String from = request.getParameter("from");
        String items = ParameterUtil.getParameter(request, "items");
        Map<Long, Long> itemCounts = new HashMap<>();
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
        if(StringUtils.isNotBlank(idCard)){
        	//验证身份证是否合法
        	if(!IdcardUtils.validateCard(idCard)){
        		welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.ID_CARD_NOT_SAME.getMsg());
                welinkVO.setCode(BizErrorEnum.ID_CARD_NOT_SAME.getCode());
                return JSON.toJSONString(welinkVO);
        	}
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
            /*if (shippingId > 0) {
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
            }*/
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
        //Long cachedCommunityId = (Long) session.getAttribute(BizConstants.SHOP_ID);
        Long cachedCommunityId = -1L;
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
        
        ObjectTaggedDO panicObjectTaggedDO = null;
        
        for(Long itemIdChk : itemIds){
        	panicObjectTaggedDO = itemService.fetchTagObjectsViaItemId(itemIdChk, BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
        	if(null != panicObjectTaggedDO){
        		if(null == panicObjectTaggedDO.getStartTime() || panicObjectTaggedDO.getStartTime().getTime() > nowDate.getTime()){
        			welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
        			//welinkVO.setMsg("亲~商品抢购未开始");
        			welinkVO.setCode(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_NOT_START.getCode());
        			welinkVO.setMsg(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_NOT_START.getMessage());
                    return JSON.toJSONString(welinkVO);
                }
                if(null == panicObjectTaggedDO.getEndTime() || panicObjectTaggedDO.getEndTime().getTime() < nowDate.getTime()){
                	welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                    //welinkVO.setMsg("亲~商品抢购结束啦");
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
        	//限购检查
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
        Long objectId = null;
        if(orderType.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//定制订单
        	objectId = mineScBoxId;
        }
        Constants.TradeFrom tradeFrom = UserUtils.differentiateOS(request);
        Profiler.enter("create trade transaction userId:" + profileId + " create trade");
        if(orderType.equals(Constants.TradeType.lotteryDraw_type.getTradeTypeId())){	//抽奖订单
        	tradeResult = appointmentService.createNewLotteryDrawTrade(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
                    tradeFrom.getTypeId(), payType, logisticsId, shippingType, appointDeliveryTime);
        }else if(orderType.equals(Constants.TradeType.crowdfund_type.getTradeTypeId())){	//众筹订单
        	tradeResult = appointmentService.createNewAppointment(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
        			tradeFrom.getTypeId(), payType, logisticsId, shippingType, 0L, -1L, appointDeliveryTime, orderType, objectId, crowdfundDetailId, scratchCardId, pUserId);
        }else{	//正常订单
        	tradeResult = appointmentService.createNewAppointment(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
        			tradeFrom.getTypeId(), payType, logisticsId, shippingType, point, couponId, appointDeliveryTime, orderType, objectId, crowdfundDetailId, scratchCardId, pUserId);
        }
        Profiler.release();
        //query new trade
        boolean tradeCreated = false;
        if (tradeResult.isSuccess() && tradeResult.getResult() != null) {
            //记录活动购买记录
            long tid = tradeResult.getResult().getId();
            Trade trade = tradeMapper.selectByPrimaryKey(tid);
            if (null != trade) {
            	resultMap.put("tradeType", trade.getType());
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
                if(!orderType.equals(Constants.TradeType.lotteryDraw_type.getTradeTypeId())){	//正常订单
	                Map<String, Long> promotion = usePromotionService.findUsePromotionByTradeId(trade.getTradeId());
	                if (promotion != null) {
	                    if (promotion.containsKey("point")) {
	                        tradeViewDO.setPoint(promotion.get("point"));
	                    }
	                    if (promotion.containsKey("coupon")) {
	                        tradeViewDO.setPoint(promotion.get("coupon"));
	                    }
	                }
                }
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
        if ((Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0 || totalPrice == 0) && tradeCreated) {
            log.info("cod pay type - trade created success......" + ",sessionId:" + session.getId().toString());
            welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
            resultMap.put("trade_id", tradeId);
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
            //减库存--在付款后减库存
//            itemService.updateStock(tradeResult.getResult());
            return JSON.toJSONString(welinkVO);
        }
        //生成支付相关 获取店铺支付宝账户
        ShopDO shop = shopService.fetchShopByShopId(shopId);
        /*if (null == shop) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getCode());
            welinkVO.setMsg(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getMsg());
            return JSON.toJSONString(welinkVO);
        }*/
        Profiler.enter("build param map for alipay ." + profileId + " build param");
        BigDecimal t = new BigDecimal(totalPrice);
        String priceStr = t.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();
        paraMap.put("total_fee", priceStr);
        paraMap.put("partner", AlipayConfig.partner);
        //paraMap.put("seller_id", shop.getAlipayAccount());
        paraMap.put("seller_id", AlipayConfig.alipayAccount);
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

        if (StringUtils.equals(from, "cart")) {
            //清除购物车--购物车已支持部分购买，所以这里是更新购物车数据
            Map<Long, Long> toUpdateItemCounts = new HashMap<>();
            for (Long id : itemCounts.keySet()) {
                toUpdateItemCounts.put(id, 0 - itemCounts.get(id));
            }
            cartService.updateItem2Cart(profileId, toUpdateItemCounts);
//            cartService.clearCart(profileId);
        }
        Profiler.release();
        EventTracker.track(mobile, "order", "order-action", "order", 1L);
        return JSON.toJSONString(welinkVO);
    }
    
    /**
     * 
     * orderDz:(定制下单). <br/>
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
    @RequestMapping(value = {"/api/m/1.0/orderDz.json"}, produces = "application/json;charset=utf-8")
    public String orderDz(HttpServletRequest request, HttpServletResponse response,
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
        //query new trade
        boolean tradeCreated = false;
        if (tradeResult.isSuccess() && tradeResult.getResult() != null) {
            //记录活动购买记录
            long tid = tradeResult.getResult().getId();
            Trade trade = tradeMapper.selectByPrimaryKey(tid);
            if (null != trade) {
            	resultMap.put("tradeType", trade.getType());
                //查找站點
                CommunityDO communityDO = communityDOMapper.selectByPrimaryKey(communityId);

                TradeViewDO tradeViewDO = ViewDOCopy.buildTradeViewDO(trade, communityDO);
                //记录购买记录
                //itemService.recordBuy(itemCounts, profileId, tradeResult.getResult().getTradeId());

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
                Map<String, Long> promotion = usePromotionService.findUsePromotionByTradeId(trade.getTradeId());
                if (promotion != null) {
                    if (promotion.containsKey("point")) {
                        tradeViewDO.setPoint(promotion.get("point"));
                    }
                    if (promotion.containsKey("coupon")) {
                        tradeViewDO.setPoint(promotion.get("coupon"));
                    }
                }
                resultMap.put("trade", tradeViewDO);
                Profiler.release();
            } else {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.CREATE_TRADE_FAILED.getCode());
                welinkVO.setMsg(BizErrorEnum.CREATE_TRADE_FAILED.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        } else {
            //log.error("create trade failed. itemId:" + itemId + ",profileId:" + profileId + ",sessionId:" + session.getId().toString());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(tradeResult.getCode());
            welinkVO.setMsg(tradeResult.getMessage());
            return JSON.toJSONString(welinkVO);
        }
        //货到付款处理
        if ((Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0 || totalPrice == 0) && tradeCreated) {
            log.info("cod pay type - trade created success......" + ",sessionId:" + session.getId().toString());
            welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
            resultMap.put("trade_id", tradeId);
            if (tradeId > 0) {	//交易分润
            	asyncEventBus.post(new ProfitEvent(tradeId, 1));	//通过事件总线 交易分润 //1=加分润；2=减分润
    		}
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }
        //生成支付相关 获取店铺支付宝账户
        ShopDO shop = shopService.fetchShopByShopId(shopId);
        Profiler.enter("build param map for alipay ." + profileId + " build param");
        BigDecimal t = new BigDecimal(totalPrice);
        String priceStr = t.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();
        paraMap.put("total_fee", priceStr);
        paraMap.put("partner", AlipayConfig.partner);
        paraMap.put("seller_id", AlipayConfig.alipayAccount);
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

        Profiler.release();
        EventTracker.track(mobile, "order", "order-action", "order", 1L);
        return JSON.toJSONString(welinkVO);
    }

    public static void main(String[] args) {
        BigDecimal t = new BigDecimal(1);
        String priceStr = t.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();
        System.out.println(priceStr);
    }
}
