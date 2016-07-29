package com.welink.web.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.constants.TimeConstants;
import com.welink.biz.common.model.CouponViewDO;
import com.welink.biz.common.model.ItemJson;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.AddressService;
import com.welink.biz.service.CouponService;
import com.welink.biz.service.ItemService;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.BuyItemResultCode;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ConsigneeAddrDO;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.MikuCrowdfundDO;
import com.welink.commons.domain.MikuCrowdfundDetailDO;
import com.welink.commons.domain.MikuMineDetectReportDO;
import com.welink.commons.domain.MikuMineScBoxDO;
import com.welink.commons.domain.MikuScratchCardDO;
import com.welink.commons.domain.ObjectTaggedDO;
import com.welink.commons.domain.PointAccountDO;
import com.welink.commons.domain.PointAccountDOExample;
import com.welink.commons.model.ItemActivityTag;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuCrowdfundDOMapper;
import com.welink.commons.persistence.MikuCrowdfundDetailDOMapper;
import com.welink.commons.persistence.MikuItemShareParaDOMapper;
import com.welink.commons.persistence.MikuMineDetectReportDOMapper;
import com.welink.commons.persistence.MikuMineScBoxDOMapper;
import com.welink.commons.persistence.MikuScratchCardDOMapper;
import com.welink.commons.persistence.PointAccountDOMapper;
import com.welink.commons.vo.MikuActiveTopicVO;
import com.welink.commons.vo.TopicParameterVO;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.thread.MaxVisitCountThread;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 14-11-11.
 */
@RestController
public class ConfirmOrder {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ConfirmOrder.class);

    @Resource
    private ItemMapper itemMapper;
    
    @Resource
    private ItemService itemService;

    @Resource
    private AddressService addressService;

    @Resource
    private CouponService couponService;

    @Resource
    private PointAccountDOMapper pointAccountDOMapper;

    @Resource
    private AppointmentTradeService appointmentService;
    
    @Resource
    private MikuItemShareParaDOMapper mikuItemShareParaDOMapper;
    
    @Resource
	private MikuCrowdfundDOMapper mikuCrowdfundDOMapper;
	
	@Resource
	private MikuCrowdfundDetailDOMapper mikuCrowdfundDetailDOMapper;
	
	@Resource
	private MikuScratchCardDOMapper mikuScratchCardDOMapper;
	
	@Resource
    private MikuMineDetectReportDOMapper mikuMineDetectReportDOMapper;
	
	@Resource
	private MikuMineScBoxDOMapper mikuMineScBoxDOMapper;
    
    @Resource
	private MemcachedClient memcachedClient; 
    
    @RequestMapping(value = {"/api/m/1.0/confirmOrder.json", "/api/h/1.0/confirmOrder.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="orderType", required = false, defaultValue="1") Byte orderType,
    		@RequestParam(value="scratchCardId", required = false, defaultValue="-1") Long scratchCardId,
    		@RequestParam(value="mineScBoxId", required = false, defaultValue="-1") Long mineScBoxId,
    		@RequestParam(value="crowdfundDetailId", required = false, defaultValue="-1") Long crowdfundDetailId) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
    	try {
	    	if(null != memcachedClient){
	    		Long currentTime = System.currentTimeMillis();
	    		String max_visit_countStr = (String)memcachedClient.get(BizConstants.MAX_VISIT_COUNT);	//已剩范围人数
	    		String max_visit_count_resetStr = (String)memcachedClient.get(BizConstants.MAX_VISIT_COUNT_RESET);	//初始设置访问人数
	    		String max_visit_timesStr = (String)memcachedClient.get(BizConstants.MAX_VISIT_TIMES);	//初始化访问人数间隔时间(秒)
	    		String max_visit_end_timeStr = (String)memcachedClient.get(BizConstants.MAX_VISIT_END_TIME);	//间隔的结束时间
	    		
	    		if( null != max_visit_countStr && StringUtils.isNumeric(max_visit_countStr)){
	    			int max_visit_count = Integer.valueOf(max_visit_countStr);
		    		if( null == max_visit_end_timeStr || !StringUtils.isNumeric(max_visit_end_timeStr)){	//设置
		    			if( null != max_visit_timesStr || StringUtils.isNumeric(max_visit_timesStr)){
		    				int max_visit_times = Integer.valueOf(max_visit_timesStr);
		    				memcachedClient.set(BizConstants.MAX_VISIT_END_TIME, 60, currentTime + max_visit_times*1000);	//设置间隔的结束时间
		    			}else{
		    				memcachedClient.set(BizConstants.MAX_VISIT_TIMES, 10*TimeConstants.REDIS_EXPIRE_DAY_1_2, "5");
		    				memcachedClient.set(BizConstants.MAX_VISIT_END_TIME, 60, currentTime + 5*1000);
		    			}
		    		}else{
		    			Long max_visit_end_time = Long.valueOf(max_visit_end_timeStr);
		    			if((currentTime - max_visit_end_time) >= 0){
		    				if( null != max_visit_timesStr || StringUtils.isNumeric(max_visit_timesStr)){
			    				int max_visit_times = Integer.valueOf(max_visit_timesStr);
			    				memcachedClient.set(BizConstants.MAX_VISIT_END_TIME, 60, currentTime + max_visit_times*1000);	//设置间隔的结束时间
			    			}else{
			    				memcachedClient.set(BizConstants.MAX_VISIT_TIMES, 10*TimeConstants.REDIS_EXPIRE_DAY_1_2, "5");
			    				memcachedClient.set(BizConstants.MAX_VISIT_END_TIME, 60, currentTime + 5*1000);
			    			}
		    			}
		    		}
		    		
		    		if( null == max_visit_count_resetStr || !StringUtils.isNumeric(max_visit_count_resetStr)){
		    			memcachedClient.set(BizConstants.MAX_VISIT_COUNT_RESET, 10*TimeConstants.REDIS_EXPIRE_DAY_1_2, "2000");	//初始设置访问人数
		    		}
		    		
		    		if( null == max_visit_timesStr || !StringUtils.isNumeric(max_visit_timesStr)){
		    			memcachedClient.set(BizConstants.MAX_VISIT_TIMES, 10*TimeConstants.REDIS_EXPIRE_DAY_1_2, "5");
		    		}
		    		
		    		if(max_visit_count > 0){		//判断访问人数是否超过可访问人数
		    			memcachedClient.decr(MaxVisitCountThread.MAX_VISIT_COUNT, 1);
		    		}else{
		    			welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
		    			welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
		    			return JSON.toJSONString(welinkVO);
		    		}
	    		}else{
	    			if( null == max_visit_count_resetStr || !StringUtils.isNumeric(max_visit_count_resetStr)){
		    			memcachedClient.set(BizConstants.MAX_VISIT_COUNT, 60, "2000");	//已剩范围人数
		    		}else{
		    			int max_visit_count_reset = Integer.valueOf(max_visit_count_resetStr);
		    			memcachedClient.set(BizConstants.MAX_VISIT_COUNT, 60, max_visit_count_reset);	//已剩范围人数
		    		}
	    			
	    		}
	    	}
    	} catch (Exception e) {
			// TODO: handle exception
		}
    	
        long profileId = -1;
        String from = ParameterUtil.getParameter(request, "from");
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        Map resultMap = new HashMap();
        Date nowDate = new Date();
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
        if (profileId == -1) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }

        resultMap.put("consigned", "0");
        resultMap.put("isTaxFree", "0");	//默认不是免税产品
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
        if(orderType.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//定制订单
        	if((null == mineScBoxId || mineScBoxId < 1)){
        		welinkVO.setStatus(0);
        		welinkVO.setMsg("亲~您未选中你需要下单的盒子~");
        		return JSON.toJSONString(welinkVO);
        	}
        	if(itemList.size() != 1){
        		welinkVO.setStatus(0);
        		welinkVO.setMsg("亲~定制产品只能下一个盒子~");
        		return JSON.toJSONString(welinkVO);
        	}
        }
        Map<Long, Integer> itemNumMap = new HashMap<Long, Integer>();
        Map<String, com.welink.commons.domain.Item> itemMap = new HashMap<>();
        long totalFee = 0l;
        List<Long> itemIds = Lists.newArrayList();
        List<Item> itemDOs = Lists.newArrayList();
        if (itemList.size() > 0) {
            for (ItemJson itemJson : itemList) {
                long itemId = itemJson.getItem_id();
                itemNumMap.put(itemId, itemJson.getNum());
                BaseResult<com.welink.commons.domain.Item> itemBaseResult = itemService.fetchItemById(itemId);
                if (null != itemBaseResult && itemBaseResult.isSuccess() && null != itemBaseResult.getResult()) {
                    com.welink.commons.domain.Item item = itemBaseResult.getResult();
                    totalFee += item.getPrice() * (long) itemJson.getNum();
                    itemMap.put(String.valueOf(itemId), item);
                    itemIds.add(itemId);
                    itemDOs.add(item);
                }
            }
            
            for(Item item : itemDOs){
        		if(item.getIsTaxFree().equals((byte)1)){	//0=不免税；1=免税
                	//免税商品
                	resultMap.put("isTaxFree", "1");	//是免税产品
                }
            }
            
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

            if(itemIds.isEmpty()){
            	welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
    			welinkVO.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
    			welinkVO.setMsg(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
                return JSON.toJSONString(welinkVO);
            }
            
            //满减结果
            long fullCutFee = 0L;
            
            //2. 收货地址
            ConsigneeAddrDO consigneeAddrDO = addressService.fetchDefConsignee(profileId);
            if (null != consigneeAddrDO) {
                resultMap.put("consignee_name", consigneeAddrDO.getReceiverName());
                resultMap.put("mobile", consigneeAddrDO.getReceiverMobile());
                resultMap.put("addr", consigneeAddrDO.getReceiver_state()+ consigneeAddrDO.getReceiverCity() + consigneeAddrDO.getReceiverDistrict() + "" + consigneeAddrDO.getReceiverAddress());
                resultMap.put("community_id", consigneeAddrDO.getCommunityId());
                resultMap.put("idCard", consigneeAddrDO.getIdCard());
                resultMap.put("consigned", "1");
            }

            //可用积分数-如果可用积分
            //计算积分和优惠券可抵扣金额-如果可用优惠券
            final List<Long> limitPointObject = appointmentService.fetchContainTagItemIdsViaItemIds(itemIds, BizConstants.SearchTagEnum.NON_POINT.getTag());
            final List<Long> limitCoupon = appointmentService.fetchContainTagItemIdsViaItemIds(itemIds, BizConstants.SearchTagEnum.NON_COUPON.getTag());
            long validCouponTotalFee = 0L;
            long validPointTotalFee = 0L;
            Long itemFee = 0L;			//商品金额
            
            if(orderType.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//定制订单
            	MikuMineScBoxDO mikuMineScBoxDO = null;		//定制盒子
                if(orderType.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//定制订单
                	if(null == mineScBoxId){
                		welinkVO.setStatus(0);
                		welinkVO.setMsg("亲~没有定制产品~");
                		return JSON.toJSONString(welinkVO);
                	}
                	mikuMineScBoxDO = mikuMineScBoxDOMapper.selectByPrimaryKey(mineScBoxId);
                	if(null == mikuMineScBoxDO || (null != mikuMineScBoxDO && null == mikuMineScBoxDO.getUserId())){
                		welinkVO.setStatus(0);
                		welinkVO.setMsg("亲~没有定制产品~");
                		return JSON.toJSONString(welinkVO);
                	}else{
                		if(!mikuMineScBoxDO.getUserId().equals(profileId)){
                			welinkVO.setStatus(0);
                    		welinkVO.setMsg("亲~此定制产品不是您的，不能下单~");
                    		return JSON.toJSONString(welinkVO);
                		}
                	}
                }
            	for (Item item : itemMap.values()) {
            		totalFee = totalFee - item.getPrice() * itemNumMap.get(item.getId()) + mikuMineScBoxDO.getPrice();
            		item.setPrice(mikuMineScBoxDO.getPrice());
            		validPointTotalFee += mikuMineScBoxDO.getPrice();
            		validCouponTotalFee += mikuMineScBoxDO.getPrice();
            	}
            }else{
            	 //满减结果
                fullCutFee = getTopicTotalValue(itemDOs, itemNumMap);
                resultMap.put("fullCutFee", fullCutFee);	//满减结果
                
            	for (Item item : itemMap.values()) {
            		Long activityPrice = null;	//抢购价格
            		ObjectTaggedDO pbOtag = itemService.fetchTagActivtyViaItemId(item.getId(), BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
            		if(null != pbOtag && null != pbOtag.getKv()){
            			ItemActivityTag itemLimitTag = JSON.parseObject(pbOtag.getKv(), ItemActivityTag.class);
            			if(null != itemLimitTag && null != itemLimitTag.getActivityPrice()){
            				activityPrice = itemLimitTag.getActivityPrice();	//抢购价格
            				totalFee = totalFee - item.getPrice() * itemNumMap.get(item.getId()) + activityPrice * itemNumMap.get(item.getId()) ;
            				item.setPrice(activityPrice);
            			}
            		}
            		if (!limitPointObject.contains(item.getId())) {	//积分
            			if(null != activityPrice){
            				validPointTotalFee += (activityPrice != null ? 
            						activityPrice : 0) * itemNumMap.get(item.getId());	//抢购价格
            			}else{
            				validPointTotalFee += (item.getPrice() != null ? item.getPrice() : 0) * itemNumMap.get(item.getId()); //平台利润
            			}
            		}
            		
            		if (!limitCoupon.contains(item.getId())) {		//优惠券
            			if(null != activityPrice){
            				validCouponTotalFee += (activityPrice != null ? 
            						activityPrice : 0) * itemNumMap.get(item.getId());	//抢购价格
            			}else{
            				validCouponTotalFee += (item.getPrice() != null ? item.getPrice() : 0) * itemNumMap.get(item.getId()); //平台利润
            			}
            		}
            		
            		/*MikuItemShareParaDO mikuItemShareParaDO = itemService.getMikuItemShareParaDOByItemId(item.getId());	//获取商品分润参数
                if (!limitPointObject.contains(item.getId())) {	//积分
                	if(null != mikuItemShareParaDO && null != mikuItemShareParaDO.getItemProfitFee() 
                			&& mikuItemShareParaDO.getItemProfitFee() > 0){
                		if(null != activityPrice){
                			validPointTotalFee += (activityPrice != null ? 
                					activityPrice : 0) * itemNumMap.get(item.getId());	//抢购价格
                		}else{
                			validPointTotalFee += (mikuItemShareParaDO.getItemProfitFee() != null ? 
                					mikuItemShareParaDO.getItemProfitFee() : 0) * itemNumMap.get(item.getId());	//平台利润
                		}
                	}else{
                		if(null != activityPrice){
                			validPointTotalFee += (activityPrice != null ? 
                					activityPrice : 0) * itemNumMap.get(item.getId());	//抢购价格
                		}else{
                			validPointTotalFee += (item.getPrice() != null ? item.getPrice() : 0) * itemNumMap.get(item.getId()); //平台利润
                		}
                	}
                }
                if (!limitCoupon.contains(item.getId())) {		//优惠券
                	if(null != mikuItemShareParaDO && null != mikuItemShareParaDO.getItemProfitFee() 
                			&& mikuItemShareParaDO.getItemProfitFee() > 0){
                		if(null != activityPrice){
                			validCouponTotalFee += (activityPrice != null ? 
                					activityPrice : 0) * itemNumMap.get(item.getId());	//抢购价格
                		}else{
                			validCouponTotalFee += (mikuItemShareParaDO.getItemProfitFee() != null ? 
                					mikuItemShareParaDO.getItemProfitFee() : 0) * itemNumMap.get(item.getId());	//平台利润
                		}
                		
                	}else{
                		if(null != activityPrice){
                			validCouponTotalFee += (activityPrice != null ? 
                					activityPrice : 0) * itemNumMap.get(item.getId());	//抢购价格
                		}else{
                			validCouponTotalFee += (item.getPrice() != null ? item.getPrice() : 0) * itemNumMap.get(item.getId()); //平台利润
                		}
                	}
                }*/
            	}
            }
            validCouponTotalFee = (validCouponTotalFee - fullCutFee) > 0L ? (validCouponTotalFee - fullCutFee) : 0L;  
            validPointTotalFee = (validPointTotalFee - fullCutFee) > 0L ? (validPointTotalFee - fullCutFee) : 0L; 
            PointAccountDOExample pointAccountDOExample = new PointAccountDOExample();
            pointAccountDOExample.createCriteria().andUserIdEqualTo(profileId).andStatusEqualTo((byte) 1);
            List<PointAccountDO> pointAccountDOs = pointAccountDOMapper.selectByExample(pointAccountDOExample);
            if (null != pointAccountDOs && pointAccountDOs.size() > 0 && validPointTotalFee <= pointAccountDOs.get(0).getAvailableBalance()) {
                resultMap.put("point", validPointTotalFee);
            } else if (null != pointAccountDOs && pointAccountDOs.size() > 0) {
                resultMap.put("point", pointAccountDOs.get(0).getAvailableBalance());
            } else {
                resultMap.put("point", 0);
            }
            
            if(orderType.equals(Constants.TradeType.crowdfund_type.getTradeTypeId())){	//众筹订单
            	MikuCrowdfundDetailDO mikuCrowdfundDetailDO = mikuCrowdfundDetailDOMapper.selectByPrimaryKey(crowdfundDetailId);
            	if(!mikuCrowdfundDetailDO.getItemId().equals(itemIds.get(0))){
            		//此商品不是当前众筹商品
            		welinkVO.setStatus(0);
                    welinkVO.setMsg(BuyItemResultCode.NOT_CROWDFUND_ITEM.getMessage());
                    welinkVO.setCode(BuyItemResultCode.NOT_CROWDFUND_ITEM.getCode());
                    return JSON.toJSONString(welinkVO);
            	}
                if(null != mikuCrowdfundDetailDO){
                	resultMap.put("crowdReturnContent", mikuCrowdfundDetailDO.getReturnContent());	//众筹明细商品回报内容
                	resultMap.put("crowdRiskTips", mikuCrowdfundDetailDO.getRiskTips());	//众筹明细商品风险提示
                	resultMap.put("crowdSpecialNote", mikuCrowdfundDetailDO.getSpecialNote());	//众筹明细商品特别说明
        			MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getCrowdfundId());
        			if(null != mikuCrowdfundDO){
        				resultMap.put("crowdPlusDay", mikuCrowdfundDO.getPlusDay());	//众筹多少天后发货
        			}else{
        				welinkVO.setStatus(0);
                        welinkVO.setMsg("亲~没有此众筹");
                        return JSON.toJSONString(welinkVO);
        			}
        			if (Constants.ApproveStatus.ON_SALE.getApproveStatusId() != mikuCrowdfundDO.getApproveStatus()) {
            			welinkVO.setStatus(0);
                        welinkVO.setMsg("亲~该众筹未上架");
                        return JSON.toJSONString(welinkVO);
            		}
            		if (null == mikuCrowdfundDO.getStatus() ||
            				(mikuCrowdfundDO.getStatus() != (byte)0)) {
            			//众筹状态(-1=无效;0=正常;1=成功;2=失败)
            			welinkVO.setStatus(0);
                        welinkVO.setMsg("亲~该众筹已结束");
                        return JSON.toJSONString(welinkVO);
            		}
            		if(mikuCrowdfundDO.getStartTime() == null
            				|| mikuCrowdfundDO.getEndTime() == null){
            			welinkVO.setStatus(0);
                        welinkVO.setMsg("亲~该众筹未开始或已过期");
                        return JSON.toJSONString(welinkVO);
            		}
            		if(mikuCrowdfundDO.getStartTime().getTime() > nowDate.getTime() 
            				|| mikuCrowdfundDO.getEndTime().getTime() < nowDate.getTime()){
            			welinkVO.setStatus(0);
                        welinkVO.setMsg("亲~该众筹未开始或已过期");
                        return JSON.toJSONString(welinkVO);
            		}
        		}
                resultMap.put("point", 0);	//众筹不能用积分和优惠券
            }else if(orderType.equals(Constants.TradeType.scratch_card.getTradeTypeId())){
            	if(null == scratchCardId || scratchCardId < 0L){
            		welinkVO.setStatus(0);
                    welinkVO.setMsg("亲~刮刮卡下单参数错误~");
                    return JSON.toJSONString(welinkVO);
            	}
            	/*if(!item.getType().equals(Constants.TradeType.scratch_card.getTradeTypeId())){
            		//商品类型不为刮刮卡商品
            		return BaseResult.failure(-999, "亲~不是刮刮卡商品不能领取~");
            	}*/
            	MikuScratchCardDO mikuScratchCardDO = mikuScratchCardDOMapper.selectByPrimaryKey(scratchCardId);
            	if(null != mikuScratchCardDO){
            		if(!mikuScratchCardDO.getUserId().equals(profileId) || mikuScratchCardDO.getIsReward().equals((byte)1)){
            			welinkVO.setStatus(0);
                        welinkVO.setMsg("亲~您未中刮刮卡奖~");
                        return JSON.toJSONString(welinkVO);
            		}
            	}else{
            		welinkVO.setStatus(0);
                    welinkVO.setMsg("亲~您未中刮刮卡奖~");
                    return JSON.toJSONString(welinkVO);
            	}
            }else{
            	//可用优惠券列表
            	if (validCouponTotalFee > 0) {
            		List<CouponViewDO> couponDOs = couponService.findAvailableCouponByUserId(profileId, validCouponTotalFee);
            		resultMap.put("coupons", couponDOs);
            	}
            }

            //是否包含0运费商品
            boolean zeroPostFee = false;
            List<ObjectTaggedDO> objectTaggedDOs = itemService.fetchTagObjectsViaItemIds(itemIds, BizConstants.SearchTagEnum.NON_POST_FEE.getTag());
            if (null != objectTaggedDOs && objectTaggedDOs.size() > 0) {
                zeroPostFee = true;
            }
            if (!orderType.equals(Constants.TradeType.crowdfund_type.getTradeTypeId()) && totalFee < Constants.POST_FEE_STEP && !zeroPostFee/*&& (consigneeAddrDO == null || consigneeAddrDO.getCommunityId() == -1)*/) {
                totalFee += Constants.POST_FEE;
                resultMap.put("post_fee", Constants.POST_FEE);
            } else {
                resultMap.put("post_fee", 0);
            }
            totalFee = (totalFee - fullCutFee) > 0L ? (totalFee - fullCutFee) : 0L;  
            resultMap.put("totalFee", totalFee);
            
            List<BuyItemViewDO> buyItemViewDOs = new ArrayList<BuyItemViewDO>();
            for (Item item : itemMap.values()) {
                BuyItemViewDO buyItemViewDO = new BuyItemViewDO();
                buyItemViewDO.setNum(itemNumMap.get(item.getId()));
                buyItemViewDO.setPrice(item.getPrice());
                buyItemViewDO.setItemId(item.getId());
                buyItemViewDO.setPics(item.getPicUrls());
                buyItemViewDO.setTitle(item.getTitle());
                buyItemViewDOs.add(buyItemViewDO);
            }
            resultMap.put("items", buyItemViewDOs);
        } else {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            return JSON.toJSONString(welinkVO);
        }
        List<Byte> payTypes = new ArrayList<>();
        //先去掉货到付款
//        payTypes.add(Constants.PayType.OFF_LINE.getPayTypeId());
        payTypes.add(Constants.PayType.ONLINE_ALIPAY.getPayTypeId());
        payTypes.add(Constants.PayType.ONLINE_WXPAY.getPayTypeId());
        if (null != itemDOs && itemDOs.size() > 0) {
            boolean containsActive = false;
            for (Item item : itemDOs) {
                if (null != item.getCategoryId() && Long.compare(item.getCategoryId(), Constants.AppointmentServiceCategory.FirstInstallActive.getCategoryId()) == 0) {
                    containsActive = true;
                }
            }
            if (containsActive) {
                payTypes.remove(0);
            }
        }

        resultMap.put("payTypes", payTypes);
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
    
    @RequestMapping(value = {"/api/m/1.0/confirmOrderDz.json", "/api/h/1.0/confirmOrderDz.json"}, produces = "application/json;charset=utf-8")
    public String confirmOrderDz(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="detectReportId", required = true) Long detectReportId) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
    	
        long profileId = -1;
        String from = ParameterUtil.getParameter(request, "from");
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        Map resultMap = new HashMap();
        Date nowDate = new Date();
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
        if (profileId == -1) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }

        resultMap.put("consigned", "0");
        resultMap.put("isTaxFree", "0");	//默认不是免税产品
        resultMap.put("postFeeStep", Constants.POST_FEE_STEP);

        if (detectReportId > 0) {
            
            resultMap.put("fullCutFee", 0L);	//满减结果
            
            //2. 收货地址
            ConsigneeAddrDO consigneeAddrDO = addressService.fetchDefConsignee(profileId);
            if (null != consigneeAddrDO) {
                resultMap.put("consignee_name", consigneeAddrDO.getReceiverName());
                resultMap.put("mobile", consigneeAddrDO.getReceiverMobile());
                resultMap.put("addr", consigneeAddrDO.getReceiver_state()+ consigneeAddrDO.getReceiverCity() + consigneeAddrDO.getReceiverDistrict() + "" + consigneeAddrDO.getReceiverAddress());
                resultMap.put("community_id", consigneeAddrDO.getCommunityId());
                resultMap.put("idCard", consigneeAddrDO.getIdCard());
                resultMap.put("consigned", "1");
            }

            //可用积分数-如果可用积分
            //计算积分和优惠券可抵扣金额-如果可用优惠券
            //final List<Long> limitPointObject = appointmentService.fetchContainTagItemIdsViaItemIds(itemIds, BizConstants.SearchTagEnum.NON_POINT.getTag());
            //final List<Long> limitCoupon = appointmentService.fetchContainTagItemIdsViaItemIds(itemIds, BizConstants.SearchTagEnum.NON_COUPON.getTag());
            long validCouponTotalFee = 0L;
            long validPointTotalFee = 0L;
            long totalFee = 0L;
            Long itemFee = 0L;			//商品金额
            
            //测肤报告
            final MikuMineDetectReportDO mikuMineDetectReportDO = mikuMineDetectReportDOMapper.selectByPrimaryKey(detectReportId);
            if(null != mikuMineDetectReportDO){
            	if(!mikuMineDetectReportDO.getUserId().equals(profileId)){
            		welinkVO.setStatus(0);
                    welinkVO.setMsg("亲~您未有测肤报告纪录，不能下单~");
                    welinkVO.setCode(-999);
                    return JSON.toJSONString(welinkVO);
            	}else{
            		if(null == mikuMineDetectReportDO.getMoney()){
            			welinkVO.setStatus(0);
                        welinkVO.setMsg("亲~此测肤报告未定价，不能下单~");
                        welinkVO.setCode(-999);
                        return JSON.toJSONString(welinkVO);
            		}
            	}
            }else{
            	welinkVO.setStatus(0);
                welinkVO.setMsg("亲~您未有测肤报告纪录，不能下单~");
                welinkVO.setCode(-999);
                return JSON.toJSONString(welinkVO);
            }
            
            if(null != mikuMineDetectReportDO.getMoney()){
            	validCouponTotalFee = Long.valueOf(mikuMineDetectReportDO.getMoney());
            	validPointTotalFee = Long.valueOf(mikuMineDetectReportDO.getMoney());
            	totalFee += Long.valueOf(mikuMineDetectReportDO.getMoney());
            }
            
            PointAccountDOExample pointAccountDOExample = new PointAccountDOExample();
            pointAccountDOExample.createCriteria().andUserIdEqualTo(profileId).andStatusEqualTo((byte) 1);
            List<PointAccountDO> pointAccountDOs = pointAccountDOMapper.selectByExample(pointAccountDOExample);
            if (null != pointAccountDOs && pointAccountDOs.size() > 0 && validPointTotalFee <= pointAccountDOs.get(0).getAvailableBalance()) {
                resultMap.put("point", validPointTotalFee);
            } else if (null != pointAccountDOs && pointAccountDOs.size() > 0) {
                resultMap.put("point", pointAccountDOs.get(0).getAvailableBalance());
            } else {
                resultMap.put("point", 0);
            }
            
        	//可用优惠券列表
        	if (validCouponTotalFee > 0) {
        		List<CouponViewDO> couponDOs = couponService.findAvailableCouponByUserId(profileId, validCouponTotalFee);
        		resultMap.put("coupons", couponDOs);
        	}

            //是否包含0运费商品
            boolean zeroPostFee = false;
            if (totalFee < Constants.POST_FEE_STEP && !zeroPostFee/*&& (consigneeAddrDO == null || consigneeAddrDO.getCommunityId() == -1)*/) {
                totalFee += Constants.POST_FEE;
                resultMap.put("post_fee", Constants.POST_FEE);
            } else {
                resultMap.put("post_fee", 0);
            }
            resultMap.put("totalFee", totalFee);
            
            /*List<BuyItemViewDO> buyItemViewDOs = new ArrayList<BuyItemViewDO>();
            for (Item item : itemMap.values()) {
                BuyItemViewDO buyItemViewDO = new BuyItemViewDO();
                buyItemViewDO.setNum(itemNumMap.get(item.getId()));
                buyItemViewDO.setPrice(item.getPrice());
                buyItemViewDO.setItemId(item.getId());
                buyItemViewDO.setPics(item.getPicUrls());
                buyItemViewDO.setTitle(item.getTitle());
                buyItemViewDOs.add(buyItemViewDO);
            }
            resultMap.put("items", null);*/
        } else {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            return JSON.toJSONString(welinkVO);
        }
        List<Byte> payTypes = new ArrayList<>();
        //先去掉货到付款
//        payTypes.add(Constants.PayType.OFF_LINE.getPayTypeId());
        payTypes.add(Constants.PayType.ONLINE_ALIPAY.getPayTypeId());
        payTypes.add(Constants.PayType.ONLINE_WXPAY.getPayTypeId());

        resultMap.put("payTypes", payTypes);
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
    
    /**
     * 查询商品列表的满减结果
     * @param items
     * @param itemNumMap
     * @return
     */
    public Long getTopicTotalValue(List<Item> items, Map<Long, Integer> itemNumMap){
    	Map<Long, Item> itemMap = new HashMap<Long, Item>();
    	if(null != items && items.size() > 0){
    		List<Long> itemIds = new ArrayList<Long>();
    		for(Item item : items){
    			itemIds.add(item.getId());
    		}
	    	Map<Long, Long> topicTotalFeeMap = new HashMap<Long, Long>();	//计算好后的专题金额
	    	//获取抢购活动标
	        List<ObjectTaggedDO> panicBuyingItemTags =  itemService.fetchPanicBuyingTagViaItemIds(itemIds, BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
	        
	        //查找专题列表
	        Map<String, Object> topicParamMap = new HashMap<String, Object>();
	        topicParamMap.put("inActive", 1);	//活动中
	        topicParamMap.put("itemIds", itemIds);
			List<MikuActiveTopicVO> mikuActiveTopicVOList = itemMapper.selectTopicVOsByItemIds(topicParamMap);
			List<MikuActiveTopicVO> mikuActiveTopicVOList2 = new ArrayList<MikuActiveTopicVO>();	//没有重复的专题列表
			//商品id对应专题Map
			Map<Long, MikuActiveTopicVO> itemIdTopicMap = new HashMap<Long, MikuActiveTopicVO>();
			//Map<Long, Long> itemId_TopicIdMap = new HashMap<Long, Long>();	//商品id(key)与专题id(value)
			if(null != mikuActiveTopicVOList && !mikuActiveTopicVOList.isEmpty()){
				for(MikuActiveTopicVO vo : mikuActiveTopicVOList){
					itemIdTopicMap.put(vo.getItemId(), vo);
					//itemId_TopicIdMap.put(vo.getItemId(), vo.getId());	//商品id(key)与专题id(value)
					if(!mikuActiveTopicVOList2.isEmpty()){
						boolean topicFlag = false;
						for(MikuActiveTopicVO vo2 : mikuActiveTopicVOList2){
							if(vo.getId().equals(vo2.getId())){
								topicFlag = true;
							}
						}
						if(!topicFlag){	//如果没有重复的加入专题列表
							mikuActiveTopicVOList2.add(vo);
						}
					}else{
						mikuActiveTopicVOList2.add(vo);
					}
				}
			}
	        for(Item item : items){
	        	boolean pbFlag = false;
	        	Long totalFee = 0L;
	        	//抢购标设置价格
	        	if(null != panicBuyingItemTags && !panicBuyingItemTags.isEmpty()){
	        		for(ObjectTaggedDO pbOtag : panicBuyingItemTags){
	        			if(item.getId().equals(pbOtag.getArtificialId()) && null != pbOtag.getKv() && !"".equals(pbOtag.getKv().trim())){
	        				ItemActivityTag itemLimitTag = JSON.parseObject(pbOtag.getKv(), ItemActivityTag.class);
	        				if(null != itemLimitTag && null != itemLimitTag.getActivityPrice()){
	        					Long activityPrice = (itemLimitTag.getActivityPrice() != null ? 
	        							itemLimitTag.getActivityPrice() : 0);	//活动价格
	        					totalFee = activityPrice * itemNumMap.get(item.getId());
	        					pbFlag=true;
	        				}
	        			}
	        		}
	        	}
	        	if(!pbFlag){
	        		totalFee = item.getPrice() * itemNumMap.get(item.getId());
	        	}
	        	if(null != itemIdTopicMap && null != itemIdTopicMap.get(item.getId())){
	            	MikuActiveTopicVO mikuActiveTopicVO = itemIdTopicMap.get(item.getId());
	            	if(null != topicTotalFeeMap && null != mikuActiveTopicVO 
	            			&& null != topicTotalFeeMap.get(mikuActiveTopicVO.getId())){
	            		Long topicTotalFee = topicTotalFeeMap.get(mikuActiveTopicVO.getId());
	            		//计算好后的商品专题金额
	            		topicTotalFeeMap.put(mikuActiveTopicVO.getId(), topicTotalFee + totalFee);
	            	}else{
	            		//计算好后的商品专题金额
	            		topicTotalFeeMap.put(mikuActiveTopicVO.getId(), totalFee);
	            	}
	            }
	        }
	        Long fullCutFee = 0L;	//总满减金额
	        if(null != mikuActiveTopicVOList2 && !mikuActiveTopicVOList2.isEmpty()){
	        	Long topicMin = null, topicValue = 0L;
				for(MikuActiveTopicVO vo : mikuActiveTopicVOList2){
					try {
						Long topicTotalFee = null == topicTotalFeeMap.get(vo.getId()) ? 0L : topicTotalFeeMap.get(vo.getId());
						List<TopicParameterVO> topicParameterVOList = JSON.parseArray(vo.getParameter(), TopicParameterVO.class);
						if(topicTotalFee > 0 && null != topicParameterVOList && !topicParameterVOList.isEmpty()){
							Collections.sort(topicParameterVOList, new Comparator<TopicParameterVO>() {
								public int compare(TopicParameterVO arg0, TopicParameterVO arg1) {
                                	Long min1 = null == arg0.getMin() ? 0L : arg0.getMin();
                                	Long min2 = null == arg1.getMin() ? 0L : arg1.getMin();
                                	int i = 0;
                                	i = min1.compareTo(min2);
                                	if(i > 0){
                                		return -1;
                                	}else{
                                		return 1;
                                	}
                                }
		                    });
							for(TopicParameterVO topicParameterVO : topicParameterVOList){
								topicMin = topicParameterVO.getMin();
								topicValue = null == topicParameterVO.getValue() ? 
										0L : topicParameterVO.getValue();
								if(null != topicMin && topicTotalFee >= topicMin){
									fullCutFee += topicValue; 	//总满减金额
									break;
								}
							}
						}
					} catch (Exception e) {
						fullCutFee = 0L;
					}
				}
			}
	    	return fullCutFee;
    	}else{
    		return 0L;
    	}
    }

    private boolean sessionObject(Session session, String key) {
        if (null == session) {
            return false;
        }
        return null != session.getAttribute(key);
    }
}
