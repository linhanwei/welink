package com.welink.web.resource;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.gson.Gson;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemCanBuy;
import com.welink.biz.common.model.ItemJson;
import com.welink.biz.common.model.OrderViewDO;
import com.welink.biz.common.model.TradeViewDO;
import com.welink.biz.common.model.WelinkAgent;
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
import com.welink.biz.wx.tenpay.RequestHandler;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.biz.wx.tenpay.util.WXUtil;
import com.welink.biz.wx.tenpay.util.XMLUtil;
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
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 15-1-13.
 */
@RestController
public class AppWxOrder {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(AppWxOrder.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private AddressService addressService;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private ShopService shopService;

    @Resource
    private CartService cartService;

    @Resource
    private ItemService itemService;
    
    @Resource
    private ConsigneeAddrDOMapper consigneeAddrDOMapper;
    
    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;
    
    @Resource
    private AsyncEventBus asyncEventBus;

    @Resource
    private Env env;

    @Resource
    private MemcachedClient memcachedClient;
    
    /**
     * 
     * execute:(这里用一句话描述这个方法的作用). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param orderType	下单类型(1=正常下单，8=抽奖下单)默认1
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/api/m/1.0/appWxOrder.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="orderType", required = false, defaultValue="1") Byte orderType,
    		@RequestParam(value="idCard", required = false) String idCard,
    		@RequestParam(value="scratchCardId", required = false, defaultValue="-1") Long scratchCardId,
    		@RequestParam(value="crowdfundDetailId", required = false, defaultValue="-1") Long crowdfundDetailId,
    		@RequestParam(value="mineScBoxId", required = false, defaultValue="-1") Long mineScBoxId,
    		@RequestParam(value="pUserId", required = false, defaultValue="0") Long pUserId) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
        String from = ParameterUtil.getParameter(request, "from");
        byte shippingType = ParameterUtil.getParameterAsByteForSpringMVC(request, "sType");
        long shippingId = ParameterUtil.getParameterAslongForSpringMVC(request, "spId", -1l);
        long point = ParameterUtil.getParameterAslongForSpringMVC(request, "point", -1l);
        long couponId = ParameterUtil.getParameterAslongForSpringMVC(request, "couponId", -1l);
        Map<Long, Long> itemCounts = new HashMap<>();
        Date appointDeliveryTime = null;
        String appwxToken = "";
        ResponseResult result = new ResponseResult();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        
        if (shippingType == 1) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SELF_PICK_NOT_SUPPORTED.getCode());
            welinkVO.setMsg(BizErrorEnum.SELF_PICK_NOT_SUPPORTED.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        
        /*if (shippingId < 1) {//非自提
            if (StringUtils.isBlank(ParameterUtil.getParameter(request, BizConstants.APPOINTMENT_DELIVERY_TIME))) {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                return JSON.toJSONString(welinkVO);
            }
            appointDeliveryTime = ParameterUtil.getParameterAsDateTimeFromLongForSpringMVC(request, BizConstants.APPOINTMENT_DELIVERY_TIME);
        }*/
        long profileId = -1;
        long itemId = -1l;
        String mobile = "";
        String title = "";
        String body = "";
        long tradeId = -1;
        long sellerId = -1;
        long shopId = -1;
        long totalPrice = 0;
        try {
            if (session == null || !sessionObject(session, "profileId")) {
                result.setStatus(ResponseStatusEnum.FAILED.getCode());
                result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                return JSON.toJSONString(welinkVO);
            }
            profileId = (long) session.getAttribute("profileId");
            mobile = (String) session.getAttribute("mobile");
        } catch (Exception e) {
            log.error("fetch paras from session failed . exp:" + e.getMessage() + ",sessionId:" + session.getId());
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        if (profileId == -1) {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
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
            } else if (consigneeAddrDO != null) {
                communityId = consigneeAddrDO.getCommunityId();
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

        //校验communityId
        //Long cachedCommunityId = (Long) session.getAttribute(BizConstants.SHOP_ID);
        Long cachedCommunityId = -1L;
        log.error("========cached c id :" + cachedCommunityId);
        log.error("======== c id :" + communityId);
        /*if (Long.compare(communityId, cachedCommunityId) != 0) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.NO_EQUAL_COMMUNITY.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_EQUAL_COMMUNITY.getMsg());
            return JSON.toJSONString(welinkVO);
        }*/

        BaseResult<Trade> tradeResult = null;
        List<ImmutablePair<Long, Integer>> itemIdNumPairList = new ArrayList<ImmutablePair<Long, Integer>>();
        List<ItemJson> itemList = new ArrayList<ItemJson>();

        String items = ParameterUtil.getParameter(request, "items");
        log.error("items :" + items + ",sessionId:" + session.getId());
        com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(items);
        for (int i = 0; i < array.size(); i++) {
            com.alibaba.fastjson.JSONObject jobj = (com.alibaba.fastjson.JSONObject) array.get(i);
            ItemJson item = JSON.toJavaObject(jobj, ItemJson.class);
            itemList.add(item);
            itemId = item.getItem_id();
            itemCounts.put(item.getItem_id(), (long) item.getNum());
        }

        if (itemList.size() < 0) {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.PARAMS_ERROR.getCode());
            result.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
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
            welinkVO.setMsg("亲~刮刮卡只能领取单种商品");
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

        List<Long> itemIds = Lists.newArrayList();
        for (ItemJson item : itemList) {
            long id = item.getItem_id();
            int quantity;
            if(orderType.equals(Constants.TradeType.lotteryDraw_type.getTradeTypeId()) ||
            		orderType.equals(Constants.TradeType.scratch_card.getTradeTypeId()) ||
            		orderType.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//抽奖订单或刮刮卡订单
            	quantity = 1;
            }else{		//正常s订单
            	quantity = item.getNum();
            }
            itemIds.add(item.getItem_id());
            itemIdNumPairList.add(ImmutablePair.of(id, quantity));
        }

        if (itemId < 0) {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.CAN_NOT_FOUND_ITEM.getCode());
            result.setMsg(BizErrorEnum.CAN_NOT_FOUND_ITEM.getMsg());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.CAN_NOT_FOUND_ITEM.getMsg());
            welinkVO.setCode(BizErrorEnum.CAN_NOT_FOUND_ITEM.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
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
                	welinkVO.setCode(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_NOT_START.getCode());
        			welinkVO.setMsg(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_NOT_START.getMessage());
                    return JSON.toJSONString(welinkVO);
                }
        	}
        }
        
        
        Map resultMap = new HashMap();
        
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

        com.welink.commons.domain.Item item = itemMapper.selectByPrimaryKey(itemId);
        if (null != item) {
            sellerId = item.getSellerId();
            shopId = item.getShopId();
        }

        Long objectId = null;
        if(orderType.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//定制订单
        	objectId = mineScBoxId;
        }
        Constants.TradeFrom tradeFrom = UserUtils.differentiateOS(request);
        Profiler.enter("create trade transaction userId:" + profileId + " create trade");
        if(orderType.equals(Constants.TradeType.lotteryDraw_type.getTradeTypeId())){	//抽奖订单
        	tradeResult = appointmentService.createNewLotteryDrawTrade(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
                    tradeFrom.getTypeId(), Constants.PayType.ONLINE_WXPAY.getPayTypeId(), logisticsId, shippingType, appointDeliveryTime);
        }else if(orderType.equals(Constants.TradeType.crowdfund_type.getTradeTypeId())){	//众筹订单
        	tradeResult = appointmentService.createNewAppointment(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
        			tradeFrom.getTypeId(), Constants.PayType.ONLINE_WXPAY.getPayTypeId(), logisticsId, shippingType, 0L, -1L, appointDeliveryTime, orderType, objectId, crowdfundDetailId, scratchCardId, pUserId);
        }else{	//正常订单
        	tradeResult = appointmentService.createNewAppointment(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
                tradeFrom.getTypeId(), Constants.PayType.ONLINE_WXPAY.getPayTypeId(), logisticsId, shippingType, point, couponId, appointDeliveryTime, orderType, objectId, crowdfundDetailId, scratchCardId, pUserId);
        }
        Profiler.release();
        //query new trade
        if (tradeResult.isSuccess() && tradeResult.getResult() != null) {
            long tid = tradeResult.getResult().getId();
            Trade trade = tradeMapper.selectByPrimaryKey(tid);
            if (null != trade) {
            	resultMap.put("tradeType", trade.getType());
                //记录购买记录
                itemService.recordBuy(itemCounts, profileId, tradeResult.getResult().getTradeId());

                if (StringUtils.equals(from, "cart")) {
                    //清除购物车--购物车已支持部分购买，所以这里是更新购物车数据
                    Map<Long, Long> toUpdateItemCounts = new HashMap<>();
                    for (Long id : itemCounts.keySet()) {
                        toUpdateItemCounts.put(id, 0 - itemCounts.get(id));
                    }
                    cartService.updateItem2Cart(profileId, toUpdateItemCounts);
                }
                tradeId = trade.getTradeId();
                totalPrice = trade.getTotalFee();
                title = trade.getTitle();
                
                if (totalPrice == 0) {
                    log.info("cod pay type - trade created success......" + ",sessionId:" + session.getId().toString());
                    welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
                    resultMap.put("trade_id", tradeId);
                    if (tradeId > 0) {	//交易分润
                    	asyncEventBus.post(new ProfitEvent(tradeId, 1));	//通过事件总线 交易分润 //1=加分润；2=减分润
                    	//profitService.profitByTradeId(tradeId);
            		}
                    welinkVO.setResult(resultMap);
                    //减库存--在付款后减库存
//                    itemService.updateStock(tradeResult.getResult());
                    return JSON.toJSONString(welinkVO);
                }
                
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
                Profiler.release();
            } else {
                result.setStatus(ResponseStatusEnum.FAILED.getCode());
                result.setErrorCode(BizErrorEnum.CREATE_TRADE_FAILED.getCode());
                result.setMsg(BizErrorEnum.CREATE_TRADE_FAILED.getMsg());
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.CREATE_TRADE_FAILED.getMsg());
                welinkVO.setCode(BizErrorEnum.CREATE_TRADE_FAILED.getCode());
                return JSON.toJSONString(welinkVO);
            }
        } else {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setMsg(tradeResult.getMessage());
            result.setErrorCode(tradeResult.getCode());
            log.error("create trade failed. itemId:" + itemId + ",profileId:" + profileId + ",sessionId:" + session.getId());
            welinkVO.setStatus(0);
            welinkVO.setMsg(tradeResult.getMessage());
            welinkVO.setCode(tradeResult.getCode());
            return JSON.toJSONString(welinkVO);
        }
        //生成支付相关 获取店铺支付宝账户
        ShopDO shop = shopService.fetchShopByShopId(shopId);
        if (null == shop) {
            result.setErrorCode(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getCode());
            result.setMsg(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getMsg());
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getMsg());
            welinkVO.setCode(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getCode());
            return JSON.toJSONString(welinkVO);
        }
        //微信支付参数
        //接收财付通通知的URL TODO:更换成自己的回调地址
        String notify_url = "http://m.7nb.com.cn/api/m/1.0/weiXinCallBack.htm";
        if (env.isProd()) {
            //notify_url = "http://120.24.102.187:8080/api/m/1.0/weiXinCallBack.htm";
        	notify_url = "http://" + BizConstants.ONLINE_DOMAIN + "/api/m/1.0/weiXinCallBack.htm";
        } else {
            notify_url = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/m/1.0/weiXinCallBack.htm";
        }
        RequestHandler prepayReqHandler = new RequestHandler();//获取prepayid的请求类

        //IOS和IOSPRE的参数不同默认设置为来自IOS
        String appId = ConstantUtil.APP_APP_ID;
        String appSecret = ConstantUtil.APP_APP_SECRET;
        String partnerId = ConstantUtil.PARTNER_ID;
        String partnerKey = ConstantUtil.PARTNER_KEY;
        String appKey = ConstantUtil.APPKEY;
        appwxToken = BizConstants.APP_TOKEN;
        WelinkAgent welinkAgent = new WelinkAgent();
        String userAgent = request.getHeader(BizConstants.USER_AGENT);
        try {
            if (org.apache.commons.lang.StringUtils.isNotBlank(userAgent)) {
                welinkAgent = JSON.parseObject(userAgent, WelinkAgent.class);
            }
        } catch (Exception e) {
            log.error("record client diploma failed. profileId:" + profileId + ",sessionId:" + session.getId());
        }

        String noncestr = WXUtil.getNonceStr();
        String timestamp = WXUtil.getTimeStamp();

        if (1==1) {
            List<NameValuePair> packageParams2 = new LinkedList<NameValuePair>();
            packageParams2.add(new BasicNameValuePair("appid", appId));	
            //packageParams2.add(new BasicNameValuePair("body", title));
            title = (title.length() > 10) ? title + "……" : title.substring(0, title.length());
            packageParams2.add(new BasicNameValuePair("body", title));	//商品描述
            packageParams2.add(new BasicNameValuePair("mch_id", partnerId));	//商户号
            packageParams2.add(new BasicNameValuePair("nonce_str", noncestr));
            //packageParams2.add(new BasicNameValuePair("notify_url", URLEncoder.encode(notify_url)));	//接收财付通通知的URL
            packageParams2.add(new BasicNameValuePair("notify_url", notify_url));	//接收财付通通知的URL
            packageParams2.add(new BasicNameValuePair("out_trade_no",String.valueOf(tradeId+"_"+(int)((Math.random()*9+1)*10000))));	//商家订单号
            packageParams2.add(new BasicNameValuePair("spbill_create_ip",request.getRemoteAddr()));	//订单生成的机器IP，指用户浏览器端IP
            packageParams2.add(new BasicNameValuePair("total_fee", String.valueOf(totalPrice)));	//商品金额,以分为单位
            //packageParams.put("fee_type", "1"); //币种，1人民币   66
            packageParams2.add(new BasicNameValuePair("trade_type", "APP"));
            
			String sign = WXUtil.genPackageSign(packageParams2);
			//packageParams.put("sign", sign); //签名
			packageParams2.add(new BasicNameValuePair("sign", sign)); //签名
			
			String entity = WXUtil.toXml(packageParams2);
            String traceid = "mytestid_001";

            //获取prepayId
            //String prepayId = prepayReqHandler.appSendPrepay(signParams, ConstantUtil.APPPREPAYURL, token);
            String appprepayUrl = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
            String prepayId = null;				//预支付id
            byte[] buf = WXUtil.httpPost(appprepayUrl, entity);		//生成预支付订单
            String resContent = new String(buf);
            Gson gson = new Gson();
            Map<String, String> map = XMLUtil.doXMLParse(resContent);
            if(null != map && "SUCCESS".equals(map.get("return_code")) && "SUCCESS".equals(map.get("result_code"))){
            	prepayId = map.get("prepay_id");
            }else{
            	 System.out.println("get prepayId err ,info =" + map.get("return_msg"));
            }
            if (StringUtils.isNotBlank(prepayId)) {
                welinkVO.setStatus(1);
                SortedMap<String, String> params = new TreeMap<String, String>();
                params.put("appid", appId);
                //params.put("appkey", appKey);
                params.put("noncestr", noncestr);
                params.put("package", "Sign=WXPay");
                params.put("partnerid", partnerId);
                params.put("prepayid", prepayId);
                params.put("timestamp", timestamp);

        		sign = WXUtil.genAppSign(params);
                
                //生成签名
                /*sign = Sha1Util.createSHA1Sign(params);
                params.put("sign", sign);*/
        		params.put("sign", sign);
                params.put("tradeid", String.valueOf(tradeId));
                welinkVO.setResult(params);
                return JSON.toJSONString(welinkVO);

            } else {
                result.setStatus(ResponseStatusEnum.FAILED.getCode());
                result.setErrorCode(BizErrorEnum.WEIXIN_PREPAY_FAILED.getCode());
                result.setMsg(BizErrorEnum.WEIXIN_PREPAY_FAILED.getMsg());
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.WEIXIN_PREPAY_FAILED.getMsg());
                welinkVO.setCode(BizErrorEnum.WEIXIN_PREPAY_FAILED.getCode());
                Profiler.release();
                EventTracker.track(mobile, "hwxOrder", "hwxOrder-action", "hwxOrder", 1L);
                Profiler.release();
                return JSON.toJSONString(welinkVO);
            }
        } else {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.WEIXIN_AUTH_FAILED.getCode());
            result.setMsg(BizErrorEnum.WEIXIN_AUTH_FAILED.getMsg());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.WEIXIN_AUTH_FAILED.getMsg());
            welinkVO.setCode(BizErrorEnum.WEIXIN_AUTH_FAILED.getCode());
            Profiler.release();
            EventTracker.track(mobile, "hwxOrder", "hwxOrder-action", "hwxOrder", 1L);
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
    }
    
    
    /**
     * 
     * appWxOrderDz:(定制下单). <br/>
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
    @RequestMapping(value = {"/api/m/1.0/appWxOrderDz.json"}, produces = "application/json;charset=utf-8")
    public String appWxOrderDz(HttpServletRequest request, HttpServletResponse response,
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
        String appwxToken = "";
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
            	resultMap.put("tradeType", trade.getType());
                tradeId = trade.getTradeId();
                totalPrice = trade.getTotalFee();
                title = trade.getTitle();
                
                if (totalPrice == 0) {
                    log.info("cod pay type - trade created success......" + ",sessionId:" + session.getId().toString());
                    welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
                    resultMap.put("trade_id", tradeId);
                    if (tradeId > 0) {	//交易分润
                    	asyncEventBus.post(new ProfitEvent(tradeId, 1));	//通过事件总线 交易分润 //1=加分润；2=减分润
                    	//profitService.profitByTradeId(tradeId);
            		}
                    welinkVO.setResult(resultMap);
                    //减库存--在付款后减库存
//                    itemService.updateStock(tradeResult.getResult());
                    return JSON.toJSONString(welinkVO);
                }
                
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
                Profiler.release();
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
            return JSON.toJSONString(welinkVO);
        }
        //生成支付相关 获取店铺支付宝账户
        ShopDO shop = shopService.fetchShopByShopId(shopId);
        if (null == shop) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getMsg());
            welinkVO.setCode(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getCode());
            return JSON.toJSONString(welinkVO);
        }
        //微信支付参数
        //接收财付通通知的URL TODO:更换成自己的回调地址
        String notify_url = "http://m.7nb.com.cn/api/m/1.0/weiXinCallBack.htm";
        if (env.isProd()) {
            //notify_url = "http://120.24.102.187:8080/api/m/1.0/weiXinCallBack.htm";
        	notify_url = "http://" + BizConstants.ONLINE_DOMAIN + "/api/m/1.0/weiXinCallBack.htm";
        } else {
            notify_url = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/m/1.0/weiXinCallBack.htm";
        }
        RequestHandler prepayReqHandler = new RequestHandler();//获取prepayid的请求类

        //IOS和IOSPRE的参数不同默认设置为来自IOS
        String appId = ConstantUtil.APP_APP_ID;
        String appSecret = ConstantUtil.APP_APP_SECRET;
        String partnerId = ConstantUtil.PARTNER_ID;
        String partnerKey = ConstantUtil.PARTNER_KEY;
        String appKey = ConstantUtil.APPKEY;
        appwxToken = BizConstants.APP_TOKEN;
        WelinkAgent welinkAgent = new WelinkAgent();
        String userAgent = request.getHeader(BizConstants.USER_AGENT);
        try {
            if (org.apache.commons.lang.StringUtils.isNotBlank(userAgent)) {
                welinkAgent = JSON.parseObject(userAgent, WelinkAgent.class);
            }
        } catch (Exception e) {
            log.error("record client diploma failed. profileId:" + profileId + ",sessionId:" + session.getId());
        }

        String noncestr = WXUtil.getNonceStr();
        String timestamp = WXUtil.getTimeStamp();

        if (1==1) {
            List<NameValuePair> packageParams2 = new LinkedList<NameValuePair>();
            packageParams2.add(new BasicNameValuePair("appid", appId));	
            //packageParams2.add(new BasicNameValuePair("body", title));
            title = (title.length() > 10) ? title + "……" : title.substring(0, title.length());
            packageParams2.add(new BasicNameValuePair("body", title));	//商品描述
            packageParams2.add(new BasicNameValuePair("mch_id", partnerId));	//商户号
            packageParams2.add(new BasicNameValuePair("nonce_str", noncestr));
            //packageParams2.add(new BasicNameValuePair("notify_url", URLEncoder.encode(notify_url)));	//接收财付通通知的URL
            packageParams2.add(new BasicNameValuePair("notify_url", notify_url));	//接收财付通通知的URL
            packageParams2.add(new BasicNameValuePair("out_trade_no",String.valueOf(tradeId+"_"+(int)((Math.random()*9+1)*10000))));	//商家订单号
            packageParams2.add(new BasicNameValuePair("spbill_create_ip",request.getRemoteAddr()));	//订单生成的机器IP，指用户浏览器端IP
            packageParams2.add(new BasicNameValuePair("total_fee", String.valueOf(totalPrice)));	//商品金额,以分为单位
            //packageParams.put("fee_type", "1"); //币种，1人民币   66
            packageParams2.add(new BasicNameValuePair("trade_type", "APP"));
            
			String sign = WXUtil.genPackageSign(packageParams2);
			//packageParams.put("sign", sign); //签名
			packageParams2.add(new BasicNameValuePair("sign", sign)); //签名
			
			String entity = WXUtil.toXml(packageParams2);
            String traceid = "mytestid_001";

            //获取prepayId
            //String prepayId = prepayReqHandler.appSendPrepay(signParams, ConstantUtil.APPPREPAYURL, token);
            String appprepayUrl = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
            String prepayId = null;				//预支付id
            byte[] buf = WXUtil.httpPost(appprepayUrl, entity);		//生成预支付订单
            String resContent = new String(buf);
            Gson gson = new Gson();
            Map<String, String> map = XMLUtil.doXMLParse(resContent);
            if(null != map && "SUCCESS".equals(map.get("return_code")) && "SUCCESS".equals(map.get("result_code"))){
            	prepayId = map.get("prepay_id");
            }else{
            	 System.out.println("get prepayId err ,info =" + map.get("return_msg"));
            }
            if (StringUtils.isNotBlank(prepayId)) {
                welinkVO.setStatus(1);
                SortedMap<String, String> params = new TreeMap<String, String>();
                params.put("appid", appId);
                //params.put("appkey", appKey);
                params.put("noncestr", noncestr);
                params.put("package", "Sign=WXPay");
                params.put("partnerid", partnerId);
                params.put("prepayid", prepayId);
                params.put("timestamp", timestamp);

        		sign = WXUtil.genAppSign(params);
                
                //生成签名
                /*sign = Sha1Util.createSHA1Sign(params);
                params.put("sign", sign);*/
        		params.put("sign", sign);
                params.put("tradeid", String.valueOf(tradeId));
                welinkVO.setResult(params);
                return JSON.toJSONString(welinkVO);

            } else {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.WEIXIN_PREPAY_FAILED.getMsg());
                welinkVO.setCode(BizErrorEnum.WEIXIN_PREPAY_FAILED.getCode());
                Profiler.release();
                EventTracker.track(mobile, "hwxOrder", "hwxOrder-action", "hwxOrder", 1L);
                Profiler.release();
                return JSON.toJSONString(welinkVO);
            }
        } else {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.WEIXIN_AUTH_FAILED.getMsg());
            welinkVO.setCode(BizErrorEnum.WEIXIN_AUTH_FAILED.getCode());
            Profiler.release();
            EventTracker.track(mobile, "hwxOrder", "hwxOrder-action", "hwxOrder", 1L);
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
    }

    private boolean sessionObject(Session session, String key) {
        if (null == session) {
            return false;
        }
        return null != session.getAttribute(key);
    }
    
}
