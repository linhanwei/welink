/**
 * Project Name:welink-web
 * File Name:MikuSalesRecord.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月2日下午7:07:42
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.BannerViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.BannerService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.MikuCrowdfundService;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.LogisticsDO;
import com.welink.commons.domain.MikuCrowdfundDO;
import com.welink.commons.domain.MikuCrowdfundDetailDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.domain.TradeExample.Criteria;
import com.welink.commons.persistence.CommunityDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.LogisticsDOMapper;
import com.welink.commons.persistence.MikuCrowdfundDOMapper;
import com.welink.commons.persistence.MikuCrowdfundDetailDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.vo.TradeCrowdfundVO;
import com.welink.web.common.filter.Profiler;

/**
 * 
 * ClassName: MikuCrowdfundAction <br/>
 * Function: TODO 众筹. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2016年2月15日 下午4:37:12 <br/>
 *
 * @author LuoGuangChun
 * @version 
 * @since JDK 1.6
 */
@RestController
public class MikuCrowdfundAction {
	
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private MikuCrowdfundService mikuCrowdfundService;
	
	@Resource
	private MikuCrowdfundDOMapper mikuCrowdfundDOMapper;
	
	@Resource
	private MikuCrowdfundDetailDOMapper mikuCrowdfundDetailDOMapper;
	
	@Resource
	private TradeMapper tradeMapper;
	
	@Resource
	private ItemMapper itemMapper;
	
	@Resource
	private ItemService itemService;
	
	@Resource
	private CommunityDOMapper communityDOMapper;
	
	@Resource
	private LogisticsDOMapper logisticsDOMapper;
	
	@Resource
    private BannerService bannerService;
	
	/**
	 * 
	 * crowdfundBanner:(众筹banner). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param moduleType
	 * @return
	 */
    @RequestMapping(value = {"/api/m/1.0/crowdfundBanner.json", "/api/h/1.0/crowdfundBanner.json"}, produces = "application/json;charset=utf-8")
    public String crowdfundBanner(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="moduleType", required = false, defaultValue="3") Integer moduleType) throws Exception {
		String key = request.getParameter("k");
		WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        Map resultMap = new HashMap();
		//banners
        Map<String, List<BannerViewDO>> bannerViews = bannerService.fetchBannersMapWitchCatch(-1L, null, moduleType);
        Map<String, List<BannerViewDO>> bannerViewDOs = new HashMap<>();
        //405 406 407 408 +409+ 410 + 411 + 416+ 417
        List<String> oldKey = Lists.newArrayList();
        oldKey.add("405");
        oldKey.add("406");
        oldKey.add("407");
        oldKey.add("408");
        oldKey.add("409");
        oldKey.add("410");
        oldKey.add("411");
        oldKey.add("416");
        oldKey.add("417");
        //兼容老版本
        if (StringUtils.isBlank(key)) {
            for (String ky : oldKey) {
                bannerViewDOs.put(ky, bannerViews.get(ky));
                resultMap.put("banners", bannerViewDOs);
                welinkVO.setStatus(1);
                welinkVO.setResult(resultMap);
            }
        } else {
            if (StringUtils.isNotBlank(key) && null != bannerViews && bannerViews.size() > 0) {
                List<String> keys = Arrays.asList(key.split(","));
                for (String ky : keys) {
                    bannerViewDOs.put(ky, bannerViews.get(ky));
                }
                resultMap.put("banners", bannerViewDOs);
                welinkVO.setStatus(1);
                welinkVO.setResult(resultMap);
            }
        }
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * getCrowdfundList:(获取众筹列表). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param orderColumn 排序字段（1=权重；2=新品；3=支持数；4=众筹率；5=众筹开始时间；6=众筹结束时间
	 * @param sortType 排序类型（1=降序desc；2=升序asc）
	 * @param timeType (1=众筹中的; 2=未开始的众筹; 3=已结束的众筹; 4=未结束的众筹; 5=上架的全部的众筹;)
	 * @param status 状态(-1=无效;0=正常;1=成功;2=失败) 默认-2查询不是无效的全部
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = {"/api/m/1.0/getCrowdfundList.json", "/api/h/1.0/getCrowdfundList.json"}, produces = "application/json;charset=utf-8")
	public String getCrowdfundList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="orderColumn", required = false, defaultValue = "1") Integer orderColumn,
			@RequestParam(value="sortType", required = false, defaultValue = "1") Integer sortType,
			@RequestParam(value="timeType", required = false, defaultValue = "1") Integer timeType,
			@RequestParam(value="status", required = false, defaultValue = "-2") Byte status,
			@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		@RequestParam(value="sz", required = false, defaultValue = "7") Integer sz) throws Exception {
		int page = pg;
        int size = sz;
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
		
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		
		String sortTypeStr = " DESC";	//排序类型
		if(sortType.equals(2)){	//2=升序asc
			sortTypeStr = " ASC";
		}else{	//降序desc
			sortTypeStr = " DESC";
		}
		
		String orderByClause = " weight DESC ";
		if(orderColumn.equals(2)){	//2=新品
			orderByClause = " date_created " + sortTypeStr;
		}else if(orderColumn.equals(3)){	//3=支持数
			orderByClause = " sold_num " + sortTypeStr;
		}else if(orderColumn.equals(4)){	//4=众筹率
			orderByClause = " total_fee/target_amount " + sortTypeStr;
		}else if(orderColumn.equals(3)){	//5=众筹开始时间
			orderByClause = " start_time " + sortTypeStr;
		}else if(orderColumn.equals(3)){	//6=众筹结束时间
			orderByClause = " end_time " + sortTypeStr;
		}else{	//1=权重
			orderByClause = " weight " + sortTypeStr;
		}
		
		List<MikuCrowdfundDO> mikuCrowdfundDOList = mikuCrowdfundService.getCrowdfundList(orderByClause, startRow, size, timeType, status);
		
		boolean hasNext = true;
        if (null != mikuCrowdfundDOList && mikuCrowdfundDOList.size() < size) {
            hasNext = false;
        }else if(null == mikuCrowdfundDOList){
            hasNext = false;
        }else{
        	hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("nowDate", new Date());
        resultMap.put("list", mikuCrowdfundDOList);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * getCrowdfundInfo:(获取众筹详情). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = {"/api/m/1.0/getCrowdfundInfo.json", "/api/h/1.0/getCrowdfundInfo.json"}, produces = "application/json;charset=utf-8")
	public String getCrowdfundInfo(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Long crowdfundId) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = null;
		/*Map resultMap = new HashMap();
		MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(crowdfundId);
		if(null != mikuCrowdfundDO && null != mikuCrowdfundDO.getStatus() && 
				mikuCrowdfundDO.getStatus().equals(Constants.CrowdfundStatus.NORMAL.getStatusId())){
			MikuCrowdfundDetailDOExample mikuCrowdfundDetailDOExample = new MikuCrowdfundDetailDOExample();
			mikuCrowdfundDetailDOExample.createCriteria().andCrowdfundIdEqualTo(crowdfundId);
			List<MikuCrowdfundDetailDO> mikuCrowdfundDetailDOList = mikuCrowdfundDetailDOMapper.selectByExample(mikuCrowdfundDetailDOExample);
			MikuCrowdfundDetailVO mikuCrowdfundDetailVO = null;
			List<MikuCrowdfundDetailVO> mikuCrowdfundDetailVOList = new ArrayList<MikuCrowdfundDetailVO>();
			List<Long> itemPriceList = new ArrayList<Long>();
			Integer totalNum = 0;
			for(MikuCrowdfundDetailDO mikuCrowdfundDetailDO : mikuCrowdfundDetailDOList){
				mikuCrowdfundDetailVO = new MikuCrowdfundDetailVO();
				BeanUtils.copyProperties(mikuCrowdfundDetailDO, mikuCrowdfundDetailVO);
				//Item item = itemMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getItemId());
				ItemExample iiExample = new ItemExample();
	            iiExample.createCriteria().andIdEqualTo(mikuCrowdfundDetailDO.getItemId());//查询商品资料
	            List<Item> tmpItems = itemMapper.selectByExample(iiExample);
	            List<ItemViewDO> tmpItemViews = itemService.combineItemTags(tmpItems);

	            for(Item item : tmpItems){
	            	itemPriceList.add(item.getPrice());
	            	totalNum += (null == item.getNum() ? 0 : item.getNum());
	            }
	            
	            if(null != tmpItemViews && !tmpItemViews.isEmpty()){
	            	mikuCrowdfundDetailVO.setItemViewDO(tmpItemViews.get(0));
	            }
				mikuCrowdfundDetailVOList.add(mikuCrowdfundDetailVO);
			}
			Long minItemPrice = 999999999999L, maxItemPrice = 0L;
			for(Long itemPrice : itemPriceList){
				if(itemPrice < minItemPrice){
					minItemPrice = itemPrice;
				}
				if(itemPrice > maxItemPrice){
					maxItemPrice = itemPrice;
				}
			}
			resultMap.put("minItemPrice", minItemPrice);
			resultMap.put("maxItemPrice", maxItemPrice);
			resultMap.put("totalNum", totalNum);
			resultMap.put("crowdfundDO", mikuCrowdfundDO);
			//resultMap.put("detailVOList", mikuCrowdfundDetailVOList);
			resultMap.put("nowDate", new Date());
		}*/
		
		resultMap = mikuCrowdfundService.getCrowdfundInfo(crowdfundId);
		if(null != resultMap){
			resultMap.put("nowDate", new Date());
			resultMap.put("detailVOList", null);
		}
		
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * getCrowdfundItemList:(获取众筹商品详情 我要支持). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param crowdfundId
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/getCrowdfundItemList.json", "/api/h/1.0/getCrowdfundItemList.json"}, produces = "application/json;charset=utf-8")
	public String getCrowdfundItemList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Long crowdfundId) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = null;
		Date nowDate = new Date();
		/*MikuCrowdfundDOExample mikuCrowdfundDOExample = new MikuCrowdfundDOExample();
		com.welink.commons.domain.MikuCrowdfundDOExample.Criteria createCriteria 
			= mikuCrowdfundDOExample.createCriteria();
		createCriteria.andStatusEqualTo(Constants.CrowdfundStatus.NORMAL.getStatusId());
		createCriteria.andStartTimeLessThanOrEqualTo(nowDate).andEndTimeGreaterThan(nowDate);
 		List<MikuCrowdfundDO> mikuCrowdfundDOList = mikuCrowdfundDOMapper.selectByExample(mikuCrowdfundDOExample);
 		if(null == mikuCrowdfundDOList || mikuCrowdfundDOList.isEmpty()){
 			welinkVO.setStatus(0);
 			welinkVO.setMsg("亲~该众筹未开始或已过期");
 	        return JSON.toJSONString(welinkVO);
 		}
		MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(crowdfundId);*/
		
		/*Map resultMap = new HashMap();
		MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(crowdfundId);
		if(null != mikuCrowdfundDO && null != mikuCrowdfundDO.getStatus() && 
				!mikuCrowdfundDO.getStatus().equals(Constants.CrowdfundStatus.DEL.getStatusId())){
			MikuCrowdfundDetailDOExample mikuCrowdfundDetailDOExample = new MikuCrowdfundDetailDOExample();
			mikuCrowdfundDetailDOExample.createCriteria().andCrowdfundIdEqualTo(crowdfundId).andApproveStatusEqualTo((byte)1);
			List<MikuCrowdfundDetailDO> mikuCrowdfundDetailDOList = mikuCrowdfundDetailDOMapper.selectByExample(mikuCrowdfundDetailDOExample);
			MikuCrowdfundDetailVO mikuCrowdfundDetailVO = null;
			List<MikuCrowdfundDetailVO> mikuCrowdfundDetailVOList = new ArrayList<MikuCrowdfundDetailVO>();
			for(MikuCrowdfundDetailDO mikuCrowdfundDetailDO : mikuCrowdfundDetailDOList){
				mikuCrowdfundDetailVO = new MikuCrowdfundDetailVO();
				BeanUtils.copyProperties(mikuCrowdfundDetailDO, mikuCrowdfundDetailVO);
				//Item item = itemMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getItemId());
				ItemExample iiExample = new ItemExample();
	            iiExample.createCriteria().andIdEqualTo(mikuCrowdfundDetailDO.getItemId());//查询商品资料
	            List<Item> tmpItems = itemMapper.selectByExample(iiExample);
	            List<ItemViewDO> tmpItemViews = itemService.combineItemTags(tmpItems);
//	            ItemViewDO itemViewDO = new ItemViewDO();
//	            if (null != tmpItemViews && tmpItemViews.size() > 0) {
//	            	itemViewDO = tmpItemViews.get(0);
//	            	if(isAgency == 1 && null != itemViewDO){		//如果是代理设置商品佣金
//	            		itemService.setBrokerageFeeInItemViewDO(itemViewDO);	//设置佣金
//	                }
//	            }
	            if(null != tmpItemViews && !tmpItemViews.isEmpty()){
	            	mikuCrowdfundDetailVO.setItemViewDO(tmpItemViews.get(0));
	            }
				mikuCrowdfundDetailVOList.add(mikuCrowdfundDetailVO);
			}
			//resultMap.put("crowdfundDO", mikuCrowdfundDO);
			resultMap.put("detailVOList", mikuCrowdfundDetailVOList);
			resultMap.put("nowDate", new Date());
		}*/
		
		resultMap = mikuCrowdfundService.getCrowdfundInfo(crowdfundId);
		if(null != resultMap){
			if(null != resultMap.get("crowdfundDO")){
				MikuCrowdfundDO mikuCrowdfundDO = (MikuCrowdfundDO) resultMap.get("crowdfundDO");
				if(null != mikuCrowdfundDO){
					resultMap.put("plusDay", mikuCrowdfundDO.getPlusDay());
					resultMap.put("crowdfundStatus", mikuCrowdfundDO.getStatus());
					
				}
			}
			resultMap.put("nowDate", new Date());
			resultMap.put("minItemPrice", null);
			resultMap.put("maxItemPrice", null);
			resultMap.put("totalNum", null);
			resultMap.put("crowdfundDO", null);
		}
		
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * getCrowdfundTradeList:(我的众筹项目，订单列表). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param buyerRate	是否评价(0=未评价;1=已评价;)
	 * @param tradeStatus
	 * @param crowdfundRefundStatus 众筹退款状态(0=正常;1=退款中;2=已退款)
	 * @param pg
	 * @param sz
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/getCrowdfundTradeList.json", "/api/h/1.0/getCrowdfundTradeList.json"}, produces = "application/json;charset=utf-8")
	public String getCrowdfundTradeList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="buyerRate", required = false) Byte buyerRate,
			@RequestParam(value="tradeStatus", required = false) String tradeStatus,
			@RequestParam(value="crowdfundRefundStatus", required = false) String crowdfundRefundStatus,
			@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		@RequestParam(value="sz", required = false, defaultValue = "7") Integer sz) throws Exception {
		int page = pg;
        int size = sz;
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
        
        long profileId = -1l;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
		
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		
		List<Byte> tradeStatusList = new ArrayList<Byte>();
		if (StringUtils.isNotBlank(tradeStatus)) {
            Profiler.enter("fetch trades with status " + tradeStatus);
            String statuses[] = tradeStatus.split(",");
            for (String status : statuses) {
                tradeStatusList.add(Byte.valueOf(status));
            }
            if (StringUtils.equalsIgnoreCase("4,5", tradeStatus)) {
                tradeStatusList.add((byte) 6);
            }
		}
		
		List<Byte> crowdfundRefundStatusList = new ArrayList<Byte>();	
		if (StringUtils.isNotBlank(crowdfundRefundStatus)) {
			String crowdfundRefundStatusArr[] = crowdfundRefundStatus.split(",");
            for (String crowdfundRefundStatusStr : crowdfundRefundStatusArr) {
            	crowdfundRefundStatusList.add(Byte.valueOf(crowdfundRefundStatusStr));
            }
		}
		
		TradeExample tradeExample = new TradeExample();
        Criteria createCriteria = tradeExample.createCriteria(); //
        createCriteria.andBuyerIdEqualTo(profileId); //
        if(null != buyerRate){
        	createCriteria.andBuyerRateEqualTo(buyerRate);
        }
        if(null != tradeStatusList && !tradeStatusList.isEmpty()){
        	createCriteria.andStatusIn(tradeStatusList);
        }
        if(null != crowdfundRefundStatusList && !crowdfundRefundStatusList.isEmpty()){
        	if(crowdfundRefundStatusList.size() > 1){
        		createCriteria.andCrowdfundRefundStatusIn(crowdfundRefundStatusList);
        	}else{
        		createCriteria.andCrowdfundRefundStatusEqualTo(crowdfundRefundStatusList.get(0));
        	}
        }
        createCriteria.andTypeEqualTo(Constants.TradeType.crowdfund_type.getTradeTypeId());	//订单类型为众筹
        tradeExample.setOrderByClause("id DESC");
        tradeExample.setOffset(startRow);
        tradeExample.setLimit(size);
        List<Trade> trades = tradeMapper.selectByExample(tradeExample);
        List<TradeCrowdfundVO> tradeCrowdfundVOList = new ArrayList<TradeCrowdfundVO>();
        
        if (null != trades) {
            List<Long> ratedTradeIds = new ArrayList<>();
            for (Trade trade : trades) {
            	//查找站點
                Long communityId = trade.getCommunityId();
                CommunityDO communityDO = communityDOMapper.selectByPrimaryKey(communityId);
                TradeCrowdfundVO tradeCrowdfundVO = ViewDOCopy.buildTradeCrowdfundVO(trade, communityDO);
                tradeCrowdfundVO.setCrowdfundRefundStatus(trade.getCrowdfundRefundStatus());	//众筹退款状态
                LogisticsDO logisticsDO = logisticsDOMapper.selectByPrimaryKey(trade.getConsigneeId());
				if(null != logisticsDO){
				    tradeCrowdfundVO.setReceiptName(logisticsDO.getContactName());	//收货人
				    tradeCrowdfundVO.setReceiptMobile(logisticsDO.getMobile());		//收货人电话
				    tradeCrowdfundVO.setReceiptAddr(logisticsDO.getAddr());			//收货人地址
				}
                
                MikuCrowdfundDetailDO mikuCrowdfundDetailDO = mikuCrowdfundDetailDOMapper.selectByPrimaryKey(trade.getCrowdfundDetailId());
                //MikuCrowdfundDetailDO mikuCrowdfundDetailDO = mikuCrowdfundDetailDOMapper.selectByPrimaryKey(1L);
                if(null != mikuCrowdfundDetailDO){
        			tradeCrowdfundVO.setCrowdDetailId(mikuCrowdfundDetailDO.getId()); 	//众筹明细id
        			tradeCrowdfundVO.setItemId(mikuCrowdfundDetailDO.getItemId());	//商品id
        			tradeCrowdfundVO.setCrowdDetailSoldNum(mikuCrowdfundDetailDO.getSoldNum());	//众筹明细支持数
        			tradeCrowdfundVO.setCrowdReturnContent(mikuCrowdfundDetailDO.getReturnContent()); //众筹明细回报内容
        			if(null != mikuCrowdfundDetailDO.getItemId()){
        				Item item = itemMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getItemId());
        				if(null != item){
        					tradeCrowdfundVO.setItemName(item.getTitle());	//商品名称
        					tradeCrowdfundVO.setItemPicUrls(item.getPicUrls());
        				}
        			}
        			MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getCrowdfundId());
        			if(null != mikuCrowdfundDO){
        				tradeCrowdfundVO.setCrowdfundId(mikuCrowdfundDO.getId()); 	//众筹id
        				tradeCrowdfundVO.setCrowdTitle(mikuCrowdfundDO.getTitle());	//众筹名称
        				tradeCrowdfundVO.setCrowdPicUrls(mikuCrowdfundDO.getPicUrls());	//众筹图片
        				tradeCrowdfundVO.setCrowdTargetAmount(mikuCrowdfundDO.getTargetAmount());	//众筹目标金额
        				tradeCrowdfundVO.setCrowdTotalFee(mikuCrowdfundDO.getTotalFee());	//众筹已筹金额
        				tradeCrowdfundVO.setCrowdEndTime(mikuCrowdfundDO.getEndTime());	//众筹结束时间
        				tradeCrowdfundVO.setCrowdSoldNum(mikuCrowdfundDO.getSoldNum());	//众筹支持数
        				tradeCrowdfundVO.setCrowdPlusDay(mikuCrowdfundDO.getPlusDay()); //众筹结束后多少天后发货
        				tradeCrowdfundVO.setCrowdStatus(mikuCrowdfundDO.getStatus());	//众筹状态(-1=无效;0=正常;1=成功;2=失败)
        			}
        		}
                
                tradeCrowdfundVOList.add(tradeCrowdfundVO);
            }
        }
		
		boolean hasNext = true;
        if (null != trades && trades.size() < size) {
            hasNext = false;
            
        } else {
            hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("nowDate", new Date());
        resultMap.put("list", tradeCrowdfundVOList);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * getCrowdfundTradeDetail:(众筹订单详情). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param tradeId
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/getCrowdfundTradeDetail.json", "/api/h/1.0/getCrowdfundTradeDetail.json"}, produces = "application/json;charset=utf-8")
	public String getCrowdfundTradeDetail(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="tradeId", required = false) Long tradeId) throws Exception {
		long profileId = -1l;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
		
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		
		TradeExample tradeExample = new TradeExample();
		tradeExample.createCriteria().andTradeIdEqualTo(tradeId).andBuyerIdEqualTo(profileId);
		List<Trade> tradeList = tradeMapper.selectByExample(tradeExample);
		if(!tradeList.isEmpty()){
			Trade trade = tradeList.get(0);
			//判断为众筹订单
			if(null != trade && null != trade.getCrowdfundDetailId() 
					&& trade.getType().equals(Constants.TradeType.crowdfund_type.getTradeTypeId())){	
				LogisticsDO logisticsDO = logisticsDOMapper.selectByPrimaryKey(trade.getConsigneeId());
				if(null != logisticsDO){
					resultMap.put("contactName", logisticsDO.getContactName());
					resultMap.put("mobile", logisticsDO.getMobile());
					resultMap.put("addr", logisticsDO.getAddr());
				}
				//众筹明细
				MikuCrowdfundDetailDO mikuCrowdfundDetailDO = mikuCrowdfundDetailDOMapper.selectByPrimaryKey(trade.getCrowdfundDetailId());
				//MikuCrowdfundDetailDO mikuCrowdfundDetailDO = mikuCrowdfundDetailDOMapper.selectByPrimaryKey(1L);
                if(null != mikuCrowdfundDetailDO){
        			resultMap.put("crowdDetailSoldNum", mikuCrowdfundDetailDO.getSoldNum()); //众筹明细支持数
        			resultMap.put("returnContent", mikuCrowdfundDetailDO.getReturnContent()); //众筹明细回报内容
        			//众筹
        			MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getCrowdfundId());
        			if(null != mikuCrowdfundDO){
        				resultMap.put("crowdEndTime", mikuCrowdfundDO.getEndTime());	//众筹结束时间
        				resultMap.put("crowdSoldNum", mikuCrowdfundDO.getSoldNum());	//众筹支持数
        				resultMap.put("crowdPlusDay", mikuCrowdfundDO.getPlusDay());	//众筹结束后多少天后发货
        			}
                }
			}
		}
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * crowdfundOrder:(众筹下单). <br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/crowdfundOrder.json", "/api/h/1.0/crowdfundOrder.json"}, produces = "application/json;charset=utf-8")
	public String crowdfundOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		// 判断文件是否为空
		if (null != profileDO) {
			
		}
		//resultMap.put("do", mikuFaceScoreExchangeDO);
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	
}

