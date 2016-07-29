/**
 * Project Name:welink-web
 * File Name:MikuReturnGoodsAction.java
 * Package Name:com.welink.web.resource
 * Date:2016年1月11日上午11:55:51
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.OrderViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.AddressService;
import com.welink.biz.service.MikuReturnGoodsService;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.ConsigneeAddrDO;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.MikuReturnGoodsDO;
import com.welink.commons.domain.MikuReturnGoodsDOExample;
import com.welink.commons.domain.MikuReturnGoodsDOExample.Criteria;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuReturnGoodsDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.utils.UpYunUtil;
import com.welink.commons.vo.MikuReturnGoodsVO;
import com.welink.web.common.constants.ResponseStatusEnum;

/**
 * ClassName:MikuReturnGoodsAction <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月11日 上午11:55:51 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class MikuReturnGoodsAction {

	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private AddressService addressService;
	
	@Resource
	private OrderMapper orderMapper;
	
	@Resource
	private TradeMapper tradeMapper;
	
	@Resource
	private ItemMapper itemMapper;
	
	@Resource
	private MikuReturnGoodsDOMapper mikuReturnGoodsDOMapper;
	
	@Resource
	private MikuReturnGoodsService mikuReturnGoodsService;
	
	/**
	 * 
	 * faceScoreExchange:(可申请退货的订单列表). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/getCanReturnOrders.json", "/api/h/1.0/getCanReturnOrders.json"}, produces = "application/json;charset=utf-8")
	public String getCanReturnOrders(HttpServletRequest request, HttpServletResponse response,
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
		//ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		
		List<Byte> returnStatus = new ArrayList<Byte>();
		returnStatus.add(Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId());	//申请退货
		returnStatus.add(Constants.ReturnGoodsStatus.RETURNNING.getStatusId());	//退货中
		returnStatus.add(Constants.ReturnGoodsStatus.FINISHED.getStatusId());		//退货完成
		returnStatus.add(Constants.ReturnGoodsStatus.RETURN_ERROR.getStatusId());	//退货异常
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("buyerId", profileId);
		paramMap.put("notInReturnStatus", returnStatus);
		paramMap.put("timeoutActionTime", new Date());
		paramMap.put("limit", size);
		paramMap.put("offset", startRow);
		paramMap.put("orderByClause", "o.date_created DESC");
		List<Order> orders = tradeMapper.getCanReturnOrders(paramMap);
		List<OrderViewDO> orderViewDOList = new ArrayList<OrderViewDO>();
		OrderViewDO orderViewDO = null;
		for(Order order : orders){
			orderViewDO = new OrderViewDO();
			orderViewDO.setTitle(order.getTitle());
			orderViewDO.setCateId(order.getCategoryId());
			orderViewDO.setPrice(order.getPrice());
			orderViewDO.setPics(order.getPicUrl());
			orderViewDO.setNum(order.getNum());
			orderViewDO.setRated(order.getBuyerRate());
			orderViewDO.setItemId(order.getArtificialId());
			//orderViewDO.setSpecification(order.get);
			orderViewDO.setId(order.getId());
			orderViewDO.setTotalFee(order.getTotalFee());
			orderViewDO.setReturnStatus(order.getReturnStatus());
			orderViewDOList.add(orderViewDO);
		}
		
		boolean hasNext = true;
        if (null != orders && orders.size() < size) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        welinkVO.setStatus(1);
        if(!orderViewDOList.isEmpty()){
        	resultMap.put("list", orderViewDOList);
        }else{
        	resultMap.put("list", null);
        }
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * getReturnOrders:(获取退货列表). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param pg
	 * @param sz
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/getReturnOrders.json", "/api/h/1.0/getReturnOrders.json"}, produces = "application/json;charset=utf-8")
	public String getReturnOrders(HttpServletRequest request, HttpServletResponse response,
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
		//ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		
		List<Byte> returnStatus = new ArrayList<Byte>();
		returnStatus.add(Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId());		//申请退货
		returnStatus.add(Constants.ReturnGoodsStatus.RETURNNING.getStatusId());		//退货中
		returnStatus.add(Constants.ReturnGoodsStatus.FINISHED.getStatusId());		//退货完成
		returnStatus.add(Constants.ReturnGoodsStatus.RETURN_ERROR.getStatusId());	//退货异常
		
		OrderExample orderExample = new OrderExample();
		orderExample.createCriteria().andBuyerIdEqualTo(profileId).andReturnStatusIn(returnStatus);
		List<Order> orders = orderMapper.selectByExample(orderExample);
		
		boolean hasNext = true;
        if (null != orders && orders.size() < size) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", orders);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * getReturnGoodList:(获取退货退款列表). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param isTimeOut 是否过期(0=未过期；1=过期)
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/getReturnGoodList.json", "/api/h/1.0/getReturnGoodList.json"}, produces = "application/json;charset=utf-8")
	public String getReturnGoodList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="status", required = false) Byte status,
			@RequestParam(value="isTimeOut", required = false) Integer isTimeOut,
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
		Date nowDate = new Date();
		//ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		
		/*OrderExample orderExample = new OrderExample();
		orderExample.createCriteria().andBuyerIdEqualTo(profileId);
		List<Order> orders = orderMapper.selectByExample(orderExample);*/
		
		MikuReturnGoodsDOExample mikuReturnGoodsDOExample = new MikuReturnGoodsDOExample();
		Criteria createCriteria = mikuReturnGoodsDOExample.createCriteria();
		createCriteria.andBuyerIdEqualTo(profileId);
		if(null != status){
			createCriteria.andStatusEqualTo(status);
		}
		if(null != isTimeOut && isTimeOut.equals(0)){	//未过期
			createCriteria.andTimeoutActionTimeGreaterThanOrEqualTo(nowDate);
		}else if(null != isTimeOut && isTimeOut.equals(1)){	//已过期
			createCriteria.andTimeoutActionTimeLessThan(nowDate);
		}
		mikuReturnGoodsDOExample.setLimit(size);
		mikuReturnGoodsDOExample.setOffset(startRow);
		mikuReturnGoodsDOExample.setOrderByClause("date_created DESC");
		List<MikuReturnGoodsDO> mikuReturnGoodsDOList = mikuReturnGoodsDOMapper.selectByExample(mikuReturnGoodsDOExample);
		
		boolean hasNext = true;
        if (null != mikuReturnGoodsDOList && mikuReturnGoodsDOList.size() < size) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", mikuReturnGoodsDOList);
        resultMap.put("nowDate", nowDate);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * getReturnGoodList:(获取退货和可退货列表). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param returnGoodsId	//退货id
	 * @param status	退款状态(-1=删除;0=正常单;1=申请退货;2=申请退货审核;3=退货中;4=退货确认收货;5=退货完成;6=退货异常)
	 * @param isTimeOut 是否过期(0=未过期；1=过期)
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/getReturnGoodsVOList.json", "/api/h/1.0/getReturnGoodsVOList.json"}, produces = "application/json;charset=utf-8")
	public String getReturnGoodsVOList(HttpServletRequest request, HttpServletResponse response,
			
			@RequestParam(value="orderId", required = false) Long orderId,
			@RequestParam(value="status", required = false) Byte status,
			@RequestParam(value="isTimeOut", required = false) Integer isTimeOut,
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
		Date nowDate = new Date();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("profileId", profileId);
		if(null != orderId && orderId > -1L){
			paramMap.put("orderId", orderId);
		}
		if(null != status){
			paramMap.put("returnStatus", status);
		}
		if(null != isTimeOut){
			paramMap.put("isTimeOut", isTimeOut);
		}
		
		paramMap.put("limit", size);
		paramMap.put("offset", startRow);
		paramMap.put("orderByClause", "o.date_created DESC");	//根据订单创建时间降序
		List<MikuReturnGoodsVO> mikuReturnGoodsVOList = mikuReturnGoodsDOMapper.getReturnGoodsVOList(paramMap);
		
		boolean hasNext = true;
        if (null != mikuReturnGoodsVOList && mikuReturnGoodsVOList.size() < size) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", mikuReturnGoodsVOList);
        resultMap.put("nowDate", nowDate);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * reqReturnGood:(确认退货). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/confirmReturnGood.json", "/api/h/1.0/confirmReturnGood.json"}, produces = "application/json;charset=utf-8")
	public String confirmReturnGood(HttpServletRequest request, HttpServletResponse response,
			Long orderId) throws Exception {
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
		Order order = orderMapper.selectByPrimaryKey(orderId);
		if(null == order){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~订单不能为空~");
			return JSON.toJSONString(welinkVO);
		}
		if(null != order && null != order.getArtificialId() && order.getArtificialId() > 0){
			if(!order.getBuyerId().equals(profileId)){	//如果订单不是当前用户的订单，则退货不成功
				welinkVO.setStatus(0);
				welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
				welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
				return JSON.toJSONString(welinkVO);
			}
		}
		
		List<Byte> returnStatus = new ArrayList<Byte>();
		returnStatus.add(Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId());	//申请退货
		returnStatus.add(Constants.ReturnGoodsStatus.RETURNNING.getStatusId());	//退货中
		returnStatus.add(Constants.ReturnGoodsStatus.FINISHED.getStatusId());		//退货完成
		
		if(returnStatus.contains(order.getReturnStatus())){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~此订单已走退货流程，不能重新退货~");
			return JSON.toJSONString(welinkVO);
		}
		
		//ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		
		resultMap.put("consigned", "0");
		//2. 收货地址
        ConsigneeAddrDO consigneeAddrDO = addressService.fetchDefConsignee(profileId);
        if (null != consigneeAddrDO) {
            resultMap.put("consignee_name", consigneeAddrDO.getReceiverName());
            resultMap.put("mobile", consigneeAddrDO.getReceiverMobile());
            resultMap.put("addr", consigneeAddrDO.getReceiver_state()+ consigneeAddrDO.getReceiverCity() + consigneeAddrDO.getReceiverDistrict() + "" + consigneeAddrDO.getReceiverAddress());
            resultMap.put("community_id", consigneeAddrDO.getCommunityId());
            resultMap.put("consigned", "1");
        }
		
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * reqReturnGood:(申请退货). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param orderId		订单id
	 * @param buyerMemo		退货原因
	 * @param buyerMemo		买家备注
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/reqReturnGood.json", "/api/h/1.0/reqReturnGood.json"}, produces = "application/json;charset=utf-8")
	public String reqReturnGood(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="returnReason", required = false, defaultValue="") String returnReason,
			@RequestParam(value="picUrl", required = false, defaultValue="") String picUrl,
			@RequestParam(value="buyerMemo", required = false, defaultValue="") String buyerMemo,
			Long orderId) throws Exception {
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
		if(null == profileDO){
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		if(null == orderId || orderId < 0){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~订单不能为空~");
			return JSON.toJSONString(welinkVO);
		}
		
		//reqReturnGood(order_id, logisticsId);
		Order order = orderMapper.selectByPrimaryKey(orderId);
		if(null == order){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~订单不能为空~");
			return JSON.toJSONString(welinkVO);
		}
		if(null != order && null != order.getArtificialId() && order.getArtificialId() > 0){
			List<Byte> returnStatus = new ArrayList<Byte>();
			returnStatus.add(Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId());	//申请退货待审核
			returnStatus.add(Constants.ReturnGoodsStatus.REQ_EXAMINE.getStatusId());//申请退货已审核
			returnStatus.add(Constants.ReturnGoodsStatus.RETURNNING.getStatusId());	//退货中
			returnStatus.add(Constants.ReturnGoodsStatus.CONFIRM_RECEIPT.getStatusId());	//退货确认收货
			returnStatus.add(Constants.ReturnGoodsStatus.FINISHED.getStatusId());	//退货完成
			//returnStatus.add(Constants.ReturnGoodsStatus.RETURN_ERROR.getStatusId());	//退货异常
			
			if(returnStatus.contains(order.getReturnStatus())){
				welinkVO.setStatus(0);
				welinkVO.setMsg("啊哦~此订单已走退货流程，不能重新退货~");
				return JSON.toJSONString(welinkVO);
			}
			
			if(!order.getBuyerId().equals(profileId)){	//如果订单不是当前用户的订单，则退货不成功
				welinkVO.setStatus(0);
				welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
				welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
				return JSON.toJSONString(welinkVO);
			}
			
			Item item = itemMapper.selectByPrimaryKey(order.getArtificialId());
			if(null != item && (null == item.getIsrefund() ||
					(null != item.getIsrefund() && !item.getIsrefund().equals((byte)1)))){
				//Isrefund是否可退货（0=不可退货;1=可退货）
				welinkVO.setStatus(0);
				welinkVO.setMsg("请~你选择的商品不能退货哟~");
				return JSON.toJSONString(welinkVO);
			}
			
			List<Byte> toDealStatus = new ArrayList<>();
			toDealStatus.add(Constants.TradeStatus.SELLER_CONSIGNED_PART.getTradeStatusId());
			toDealStatus.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
			toDealStatus.add(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
			toDealStatus.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());
			toDealStatus.add(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
			TradeExample tradeExample = new TradeExample();
			tradeExample.createCriteria().andTradeIdEqualTo(order.getTradeId())
				.andTimeoutActionTimeGreaterThan(new Date());
			List<Byte> notSendGoodsStatus = new ArrayList<Byte>();	//卖家还未发货的状态
	        notSendGoodsStatus.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());	//等待卖家发货,即:买家已付款
	        //notSendGoodsStatus.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());	//已备货
			List<Trade> trades = tradeMapper.selectByExample(tradeExample);
			if(null != trades && !trades.isEmpty()){
				Trade trade = trades.get(0);
				if(toDealStatus.contains(trade.getStatus())){
					WelinkVO reqReturnGood = null;
					if(notSendGoodsStatus.contains(trade.getStatus())){
						//卖家还未发货的状态申请退货流程
						reqReturnGood = mikuReturnGoodsService.reqReturnGoodSendAgo(order, trade.getStatus(), buyerMemo, returnReason, picUrl);
					}else{
						//卖家已发货的状态申请退货流程
						reqReturnGood = mikuReturnGoodsService.reqReturnGoodSendAfter(order, trade.getStatus(), buyerMemo, returnReason, picUrl);
					}
					//申请退货流程
					return JSON.toJSONString(reqReturnGood);
				}else{
					welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
	                welinkVO.setMsg("啊哦~此订单未付款或已完成，不能退货，请走取消订单流程~");
	                return JSON.toJSONString(welinkVO);
				}
			}
		}
		
		welinkVO.setStatus(0);
		welinkVO.setMsg("啊哦~订单不能为空~");
		return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * reqReturnGoodAbnormal:(异常申请退货). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param returnGoodId	退货id
	 * @param buyerMemo		买家备注
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/reqReturnGoodAbnormal.json", "/api/h/1.0/reqReturnGoodAbnormal.json"}, produces = "application/json;charset=utf-8")
	public String reqReturnGoodAbnormal(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="picUrl", required = false, defaultValue="") String picUrl,
			@RequestParam(value="buyerMemo", required = false, defaultValue="") String buyerMemo,
			Long returnGoodId) throws Exception {
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
		MikuReturnGoodsDO mikuReturnGoodsDO = mikuReturnGoodsDOMapper.selectByPrimaryKey(returnGoodId);
		if(null != mikuReturnGoodsDO && mikuReturnGoodsDO.getBuyerId().equals(profileId)
				&& mikuReturnGoodsDO.getStatus().equals(Constants.ReturnGoodsStatus.RETURN_ERROR.getStatusId())){
			Date timeoutActionTime = mikuReturnGoodsDO.getTimeoutActionTime();
			Long timeoutActionTimes = (null == timeoutActionTime ? 0L : timeoutActionTime.getTime());
			if(timeoutActionTimes > (new Date()).getTime()){
				//走退款异常申请退款流程
				//mikuReturnGoodsService.reqReturnGoodAbnormal(mikuReturnGoodsDO, buyerMemo);
				TradeExample tradeExample = new TradeExample();
				tradeExample.createCriteria().andTradeIdEqualTo(mikuReturnGoodsDO.getTradeId())
					.andTimeoutActionTimeGreaterThan(new Date());
				List<Trade> trades = tradeMapper.selectByExample(tradeExample);
				List<Byte> notSendGoodsStatus = new ArrayList<Byte>();	//卖家还未发货的状态
				notSendGoodsStatus.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());	//等待卖家发货,即:买家已付款
				//notSendGoodsStatus.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());	//已备货
				Order order = orderMapper.selectByPrimaryKey(mikuReturnGoodsDO.getOrderId());
				if(null == order){
					welinkVO.setStatus(0);
					welinkVO.setMsg("啊哦~订单不能为空~");
					return JSON.toJSONString(welinkVO);
				}
				
				Item item = itemMapper.selectByPrimaryKey(order.getArtificialId());
				if(null != item && null != item.getIsrefund() && !item.getIsrefund().equals((byte)1)){
					//Isrefund是否可退货（0=不可退货;1=可退货）
					welinkVO.setStatus(0);
					welinkVO.setMsg("请~你选择的商品不能退货哟~");
					return JSON.toJSONString(welinkVO);
				}
				
				if(null != trades && !trades.isEmpty()){
					Trade trade = trades.get(0);
					List<Byte> toDealStatus = new ArrayList<>();
					toDealStatus.add(Constants.TradeStatus.SELLER_CONSIGNED_PART.getTradeStatusId());
					toDealStatus.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
					toDealStatus.add(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
					toDealStatus.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());
					toDealStatus.add(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
					if(toDealStatus.contains(trade.getStatus())){
						WelinkVO reqReturnGood = null;
						if(notSendGoodsStatus.contains(trade.getStatus())){
							//卖家还未发货的状态申请退货流程
							reqReturnGood = mikuReturnGoodsService.reqReturnGoodSendAgo(order, trade.getStatus(), buyerMemo, null, picUrl);
						}else{
							//卖家已发货的状态申请退货流程
							reqReturnGood = mikuReturnGoodsService.reqReturnGoodSendAfter(order, trade.getStatus(), buyerMemo, null, picUrl);
						}
						//申请退货流程
						return JSON.toJSONString(reqReturnGood);
					}else{
						welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
		                welinkVO.setMsg("啊哦~此订单未付款或已完成，不能退货，请走取消订单流程~");
		                return JSON.toJSONString(welinkVO);
					}
				}else{
					welinkVO.setStatus(0);
					welinkVO.setMsg("啊哦~该退货订单已超时(七天)，不能退货");
					return JSON.toJSONString(welinkVO);
				}
			}else{
				welinkVO.setStatus(0);
				welinkVO.setMsg("啊哦~该退货订单已超时(七天)，不能退货");
				return JSON.toJSONString(welinkVO);
			}
			
		}else{
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~没有退货订单");
			return JSON.toJSONString(welinkVO);
		}
		//return null;
	}
	
	/**
	 * 
	 * expressReturnGood:(退货发送快递). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param returnGoodId
	 * @param expressName	快递公司
	 * @param expressNo		运单号
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/expressReturnGood.json", "/api/h/1.0/expressReturnGood.json"}, produces = "application/json;charset=utf-8")
	public String expressReturnGood(HttpServletRequest request, HttpServletResponse response,
			Long returnGoodId, String expressCompany, String expressNo) throws Exception {
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
		if(null == profileDO){
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		
		if(null == returnGoodId || returnGoodId < 0){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~没有退货单~");
			return JSON.toJSONString(welinkVO);
		}
		
		MikuReturnGoodsDO mikuReturnGoodsDO = mikuReturnGoodsDOMapper.selectByPrimaryKey(returnGoodId);
		if(null != mikuReturnGoodsDO){
			//申请退货流程
			return JSON.toJSONString(mikuReturnGoodsService.expressReturnGood(mikuReturnGoodsDO, expressCompany, expressNo));
		}else{
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~没有退货单~");
			return JSON.toJSONString(welinkVO);
		}
	}
	
	/**
	 * 
	 * uploadReturnGoodsPic:(退货上传图片). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param file
	 * @return
	 * @throws Exception
	 * @since JDK 1.6
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/uploadReturnGoodsPic.json", "/api/h/1.0/uploadReturnGoodsPic.json"}, produces = "application/json;charset=utf-8")
	public String uploadReturnGoodsPic(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "file", required=true) MultipartFile file // 关键就是这句话起了作用
			) throws Exception {
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
		if(!UpYunUtil.isImage(file)){
			//判断是否是图片
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~请选择正确的图片~");
			return JSON.toJSONString(welinkVO);
		}
		String picUrls = "";
		int countPic = 0;
		// 判断文件是否为空
		if (null != file && !file.isEmpty()) {
			try {
				String picUrl = "rg"+System.currentTimeMillis()+ profileId +".jpg";
				String dir = UpYunUtil.RETURNGOODS_DIR_ROOT;	//上传目录
				byte[] bytes = file.getBytes();
				if(UpYunUtil.writePicByMultipartFile(file, dir, picUrl, null)){	//上传
					picUrls += UpYunUtil.UPYUN_URL+dir+picUrl;
					countPic++;
				}
			} catch (Exception e) {
				e.printStackTrace();
				welinkVO.setStatus(0);
				welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
				welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
				return JSON.toJSONString(welinkVO);
			}
		}
		if("".equals(picUrls.trim())){
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
			welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		welinkVO.setStatus(1);
		resultMap.put("picUrl", picUrls);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	public static void main(String[] args) {
		List<Byte> toDealStatus = new ArrayList<>();
        toDealStatus.add(Constants.TradeStatus.SELLER_CONSIGNED_PART.getTradeStatusId());
        toDealStatus.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
        toDealStatus.add(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
        toDealStatus.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());
        toDealStatus.add(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
        
        Trade trade = new Trade();
        trade.setStatus(Constants.TradeStatus.SELLER_CONSIGNED_PART.getTradeStatusId());
        /*if(toDealStatus.contains(trade.getStatus())){
        	System.out.println("111111111111111111111111111111111111111111");
        }else{
        	System.out.println("22222222222222222222222222222222222");
        }*/
	}
	
}

