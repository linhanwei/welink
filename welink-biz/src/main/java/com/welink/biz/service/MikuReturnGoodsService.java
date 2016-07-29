/**
 * Project Name:welink-biz
 * File Name:MikuReturnGoodsService.java
 * Package Name:com.welink.biz.service
 * Date:2016年1月11日下午2:41:47
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.biz.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.mysql.fabric.xmlrpc.base.Array;
import com.mysql.jdbc.Connection;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.ConsigneeAddrDO;
import com.welink.commons.domain.LogisticsDO;
import com.welink.commons.domain.MikuReturnGoodsDO;
import com.welink.commons.domain.MikuReturnGoodsDOExample;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.MikuSalesRecordDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.LogisticsDOMapper;
import com.welink.commons.persistence.MikuReturnGoodsDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;

/**
 * ClassName:MikuReturnGoodsService <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月11日 下午2:41:47 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@Service
public class MikuReturnGoodsService implements InitializingBean {
	
	static Logger log = LoggerFactory.getLogger(MikuReturnGoodsService.class);
	
	@Resource
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private MikuReturnGoodsDOMapper mikuReturnGoodsDOMapper;
	
	@Resource
	private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
	
	@Resource
	private OrderMapper orderMapper;
	
	@Resource
	private TradeMapper tradeMapper;
	
	@Resource
	private AddressService addressService;
	
	@Resource
	private LogisticsDOMapper logisticsDOMapper;
	
	
	/**
	 * 
	 * reqReturnGoodSendAfter:(申请退货发货后的). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param order
	 * @param tradeStatus 交易状态
	 * @param buyerMemo	买家退货留言
	 * @param returnReason	买家退货原因
	 * @return
	 */
	public WelinkVO reqReturnGoodSendAfter(final Order order, final Byte tradeStatus, final String buyerMemo, final String returnReason, final String picUrl) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						Map resultMap = new HashMap();
						welinkVO.setStatus(1);
						Date now = new Date();
						Date now7 =	TimeUtils.addDay(now, 7); //加7天时间 
						long consigneeId = -1l;
				        long communityId = -1l;
				        long logisticsId = -1l;
				        boolean isAgency = false;	//当购买者是代理商时（ture=是；false=不是代理商）
				        
				        MikuReturnGoodsDOExample mikuReturnGoodsDOExampleDel = new MikuReturnGoodsDOExample();
				        mikuReturnGoodsDOExampleDel.createCriteria().andOrderIdEqualTo(order.getId());
				        	//.andStatusNotEqualTo(Constants.ReturnGoodsStatus.DEL.getStatusId());
				        List<MikuReturnGoodsDO> mikuReturnGoodsDOList = mikuReturnGoodsDOMapper.selectByExample(mikuReturnGoodsDOExampleDel);
				        if(!mikuReturnGoodsDOList.isEmpty()){
				        	//若是异常申请退货，逻辑删除之前的申请退货
				        	MikuReturnGoodsDO mikuReturnGoodsDO = new MikuReturnGoodsDO();
				        	mikuReturnGoodsDO.setStatus(Constants.ReturnGoodsStatus.DEL.getStatusId());	
							if(mikuReturnGoodsDOMapper.updateByExampleSelective(mikuReturnGoodsDO, mikuReturnGoodsDOExampleDel) < 1){
								log.info("更新退款表的记录为删除状态时失败!");
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
								welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
								return welinkVO;
							}
				        }
				        
				        List<Byte> notSendGoodsStatus = new ArrayList<Byte>();	//卖家还未发货的状态
				        notSendGoodsStatus.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());	//等待卖家发货,即:买家已付款
				        //notSendGoodsStatus.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());	//已备货
						Byte returnStatus = Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId();	//申请退货
						//returnStatus = Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId();	
				        if(notSendGoodsStatus.contains(tradeStatus)){
							//未发货的order直接更新为退货中
				        	//returnStatus = Constants.ReturnGoodsStatus.RETURNNING.getStatusId();	//退货中
						}else{
							//已发货的order更新为申请退货
					        //寄货地址
							ConsigneeAddrDO consigneeAddrDO = addressService.fetchDefConsignee(order.getBuyerId());
							if (consigneeAddrDO != null) {
					            communityId = consigneeAddrDO.getCommunityId();
					            logisticsId = addressService.addLogistics(consigneeAddrDO, -1L, (byte)1);
					            if (logisticsId < 0) {
					                welinkVO.setStatus(0);
					                welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
					                welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
					                return welinkVO;
					            }
					        } else {
					            welinkVO.setStatus(0);
					            welinkVO.setCode(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getCode());
					            welinkVO.setMsg(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getMsg());
					            return welinkVO;
					        }
						}
				        OrderExample orderExampleTradeId = new OrderExample();
				        orderExampleTradeId.createCriteria().andTradeIdEqualTo(order.getTradeId());
				        List<Order> orderTradeIdList = orderMapper.selectByExample(orderExampleTradeId);
				        List<Long> orderIdList = new ArrayList<Long>();	//交易下所有订单
				        Long orderId = null;
				        if(null != orderIdList && !orderTradeIdList.isEmpty()){
				        	for(Order order2 : orderTradeIdList){
				        		if(null != order2 && order2.getArtificialId() > 0){
				        			orderIdList.add(order2.getId());
				        		}
				        	}
				        }
				        List<Byte> returnStatusList = new ArrayList<Byte>();
				        returnStatusList.add(Constants.ReturnGoodsStatus.DEL.getStatusId());
				        returnStatusList.add(Constants.ReturnGoodsStatus.NORMAL.getStatusId());
				        returnStatusList.add(Constants.ReturnGoodsStatus.RETURN_ERROR.getStatusId());
				        MikuReturnGoodsDOExample mikuReturnGoodsDOExample = new MikuReturnGoodsDOExample();
				        mikuReturnGoodsDOExample.createCriteria().andTradeIdEqualTo(order.getTradeId())
				        	.andStatusNotIn(returnStatusList);
				        if(mikuReturnGoodsDOMapper.countByExample(mikuReturnGoodsDOExample) >= (orderIdList.size()-1)){
				        	TradeExample tradeExample = new TradeExample();
				        	tradeExample.createCriteria().andTradeIdEqualTo(order.getTradeId());
				        	Trade trade = new Trade();
				        	trade.setReturnStatus((byte)1);	//交易退货状态(0=正常单; 1=退货中; 2=已退货)
				        	//trade.setStatus(Constants.TradeStatus.TRADE_RETURNNING.getTradeStatusId()); 	//退款中
				        	trade.setTimeoutActionTime(now7);
				        	//如果交易内全部订单都已退货，则更新交易表为退款中
				        	if(tradeMapper.updateByExampleSelective(trade, tradeExample) != 1){
				        		log.info("更新交易表为退款中时失败!");
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
								welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
								return welinkVO;
				        	}
				        }else{
				        	if(tradeStatus.equals(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId())){
				        		//如果确认收货时申请的退货
				        		TradeExample tradeExample = new TradeExample();
					        	tradeExample.createCriteria().andTradeIdEqualTo(order.getTradeId());
					        	Trade trade = new Trade();
					        	trade.setTimeoutActionTime(now7);
					        	//如果交易内全部订单都已退货，则更新交易表为退款中
					        	if(tradeMapper.updateByExampleSelective(trade, tradeExample) != 1){
					        		log.info("更新交易表的过期时间时失败!");
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
									welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
									return welinkVO;
					        	}
				        	}
				        }
				        
				        /*OrderExample orderExample = new OrderExample();
				        orderExample.createCriteria().andTradeIdEqualTo(order.getTradeId())
				        	.andReturnStatusEqualTo(Constants.ReturnGoodsStatus.NORMAL.getStatusId());
				        List<Order> orderList = orderMapper.selectByExample(orderExample);*/
						MikuReturnGoodsDO mikuReturnGoodsDO = null;
						//对购买者进行退货
						mikuReturnGoodsDO = new MikuReturnGoodsDO();
						mikuReturnGoodsDO.setConsigneeId(logisticsId);	//物流id
						mikuReturnGoodsDO.setRefundFee(order.getTotalFee());;	//已分润金额
						mikuReturnGoodsDO = setReqMikuReturnGoodsDO(order, mikuReturnGoodsDO);
						mikuReturnGoodsDO.setBuyerMemo(buyerMemo);
						mikuReturnGoodsDO.setReturnReason(returnReason);
						mikuReturnGoodsDO.setStatus(returnStatus);	//申请退货
						mikuReturnGoodsDO.setIsSubsidy((byte)0);	//是否补贴(0=不补贴; 1=补贴)
						mikuReturnGoodsDO.setTradeStatus(tradeStatus);	//退货时订单状态
						mikuReturnGoodsDO.setPicUrl(picUrl);
						
						//插入退货记录
						if(mikuReturnGoodsDOMapper.insertSelective(mikuReturnGoodsDO) < 1){
							log.info("更新退款表的记录为申请退款时失败!");
							transactionStatus.setRollbackOnly();
							welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
						}
						
						//更新miku_sales_record表状态为申请退货或退货中 mikuSalesRecordDOMapper
						/*MikuSalesRecordDOExample mikuSalesRecordDOExample = new MikuSalesRecordDOExample();
						mikuSalesRecordDOExample.createCriteria().andTradeIdEqualTo(order.getTradeId())
							.andItemIdEqualTo(order.getArtificialId());
							//.andReturnStatusEqualTo(Constants.ReturnGoodsStatus.NORMAL.getStatusId());	//正常分润记录
						MikuSalesRecordDO mikuSalesRecordDO = new MikuSalesRecordDO();
						mikuSalesRecordDO.setIsGetpay(Constants.GetPayStatus.NOGETPAY.getStatusId());		
						mikuSalesRecordDO.setReturnStatus(returnStatus);	//退货状态
						mikuSalesRecordDO.setTimeoutActionTime(now7);
						if(mikuSalesRecordDOMapper.updateByPrimaryKeySelective(mikuSalesRecordDO) < 0){
							log.info("更新分润记录表退货状态时失败");
							transactionStatus.setRollbackOnly();
							welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
						}*/
						
						//更新t_order表的状态为申请退货 orderMapper
						order.setReturnStatus(returnStatus);	//退货状态
						if(orderMapper.updateByPrimaryKeySelective(order) != 1){
							log.info("更新订单退款状态为申请退款时失败");
							transactionStatus.setRollbackOnly();
							welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
						}
						
						resultMap.put("vo", mikuReturnGoodsDO);
						welinkVO.setResult(resultMap);
						return welinkVO;
					}
				});
	}
	
	/**
	 * 
	 * reqReturnGoodSendAgo:(申请退货发货前的). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param trade
	 * @param order
	 * @param tradeStatus
	 * @param buyerMemo
	 * @param returnReason
	 * @return
	 */
	public WelinkVO reqReturnGoodSendAgo(final Order order, final Byte tradeStatus, final String buyerMemo, final String returnReason, final String picUrl) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						Map resultMap = new HashMap();
						welinkVO.setStatus(1);
						Date now = new Date();
						Date now7 =	TimeUtils.addDay(now, 7); //加7天时间 
						long consigneeId = -1l;
				        long communityId = -1l;
				        long logisticsId = -1l;
				        boolean isAgency = false;	//当购买者是代理商时（ture=是；false=不是代理商）
				        
				        MikuReturnGoodsDOExample mikuReturnGoodsDOExample = new MikuReturnGoodsDOExample();
				        mikuReturnGoodsDOExample.createCriteria().andTradeIdEqualTo(order.getTradeId());
				        	//.andStatusNotEqualTo(Constants.ReturnGoodsStatus.DEL.getStatusId());
				        List<MikuReturnGoodsDO> mikuReturnGoodsDOList = mikuReturnGoodsDOMapper.selectByExample(mikuReturnGoodsDOExample);
				        if(!mikuReturnGoodsDOList.isEmpty()){
				        	//若是异常申请退货，逻辑删除之前的申请退货
				        	MikuReturnGoodsDO mikuReturnGoodsDO = new MikuReturnGoodsDO();
				        	mikuReturnGoodsDO.setStatus(Constants.ReturnGoodsStatus.DEL.getStatusId());	
							if(mikuReturnGoodsDOMapper.updateByExampleSelective(mikuReturnGoodsDO, mikuReturnGoodsDOExample) < 1){
								log.info("更新退款表的记录为删除状态时失败!");
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
								welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
								return welinkVO;
							}
				        }
				        
				        List<Byte> notSendGoodsStatus = new ArrayList<Byte>();	//卖家还未发货的状态
				        notSendGoodsStatus.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());	//等待卖家发货,即:买家已付款
				        //notSendGoodsStatus.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());	//已备货
						Byte returnStatus = Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId();	//申请退货
						//returnStatus = Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId();	
				        if(notSendGoodsStatus.contains(tradeStatus)){
				        	OrderExample orderExampleTradeId = new OrderExample();
				        	orderExampleTradeId.createCriteria().andTradeIdEqualTo(order.getTradeId());
				        	List<Order> orderTradeIdList = orderMapper.selectByExample(orderExampleTradeId);
				        	List<Long> orderIdList = new ArrayList<Long>();	//交易下所有订单
				        	Long orderId = null;
				        	if(null != orderTradeIdList && !orderTradeIdList.isEmpty()){
				        		for(Order order2 : orderTradeIdList){
				        			if(null != order2 && order2.getArtificialId() > 0){
				        				orderIdList.add(order2.getId());
				        				MikuReturnGoodsDO mikuReturnGoodsDO = null;
				        				//对购买者进行退货
				        				mikuReturnGoodsDO = new MikuReturnGoodsDO();
				        				mikuReturnGoodsDO.setConsigneeId(logisticsId);	//物流id
				        				mikuReturnGoodsDO.setRefundFee(order2.getTotalFee());;	//已分润金额
				        				mikuReturnGoodsDO = setReqMikuReturnGoodsDO(order2, mikuReturnGoodsDO);
				        				mikuReturnGoodsDO.setBuyerMemo(buyerMemo);
				        				mikuReturnGoodsDO.setReturnReason(returnReason);
				        				mikuReturnGoodsDO.setStatus(returnStatus);	//申请退货
				        				mikuReturnGoodsDO.setIsSubsidy((byte)0);	//是否补贴(0=不补贴; 1=补贴)
				        				mikuReturnGoodsDO.setTradeStatus(tradeStatus);	//退货时订单状态
				        				mikuReturnGoodsDO.setPicUrl(picUrl);
				        				//插入退货记录
				        				if(mikuReturnGoodsDOMapper.insertSelective(mikuReturnGoodsDO) < 1){
				        					log.info("更新退款表的记录为申请退款时失败!");
				        					transactionStatus.setRollbackOnly();
				        					welinkVO.setStatus(0);
				        					welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
				        					welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
				        					return welinkVO;
				        				}
				        				
				        				//更新t_order表的状态为申请退货 orderMapper
				        				order2.setReturnStatus(returnStatus);	//退货状态
				        				if(orderMapper.updateByPrimaryKeySelective(order2) != 1){
				        					log.info("更新订单退款状态为申请退款时失败");
				        					transactionStatus.setRollbackOnly();
				        					welinkVO.setStatus(0);
				        					welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
				        					welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
				        					return welinkVO;
				        				}
				        				//resultMap.put("vo", mikuReturnGoodsDO);
				        			}
				        		}
				        	}
				        	TradeExample tradeExample = new TradeExample();
				        	tradeExample.createCriteria().andTradeIdEqualTo(order.getTradeId());
				        	Trade trade = new Trade();
				        	trade.setReturnStatus((byte)1);	//交易退货状态(0=正常单; 1=退货中; 2=已退货)
				        	//trade.setStatus(Constants.TradeStatus.TRADE_RETURNNING.getTradeStatusId()); 	//退款中
				        	trade.setTimeoutActionTime(now7);
				        	//如果交易内全部订单都已退货，则更新交易表为退款中
				        	if(tradeMapper.updateByExampleSelective(trade, tradeExample) != 1){
				        		log.info("更新交易表为退款中时失败!");
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
								welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
								return welinkVO;
				        	}
						}else{
							welinkVO.setStatus(0);
							welinkVO.setMsg("啊哦~请选择发货前的订单~");
							return welinkVO;
						}
						
						welinkVO.setResult(resultMap);
						return welinkVO;
					}
				});
	}
	
	private MikuReturnGoodsDO setReqMikuReturnGoodsDO(Order order,
			MikuReturnGoodsDO mikuReturnGoodsDO) {
		if(null != mikuReturnGoodsDO && null != order){
			Date now = new Date();
			mikuReturnGoodsDO.setVersion(0L);
			mikuReturnGoodsDO.setArtificialId(order.getArtificialId());
			mikuReturnGoodsDO.setBuyerId(order.getBuyerId());
			mikuReturnGoodsDO.setDateCreated(now);
			mikuReturnGoodsDO.setLastUpdated(now);
			mikuReturnGoodsDO.setOrderId(order.getId());
			mikuReturnGoodsDO.setTradeId(order.getTradeId());
			mikuReturnGoodsDO.setPicUrl(order.getPicUrl());
			mikuReturnGoodsDO.setPrice(order.getPrice());
			mikuReturnGoodsDO.setTotalFee(order.getTotalFee());
			mikuReturnGoodsDO.setTitle(order.getTitle());
			mikuReturnGoodsDO.setTimeoutActionTime(now);
			return mikuReturnGoodsDO;
		}
		return null;
	}
	
	/**
	 * 
	 * reqReturnGoodAbnormal:(异常申请退货). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param order
	 * @param tradeStatus 交易状态
	 * @return
	 */
	public WelinkVO reqReturnGoodAbnormal(final MikuReturnGoodsDO mikuReturnGoodsDO, final String buyerMemo) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						Map resultMap = new HashMap();
						welinkVO.setStatus(1);
						Date now = new Date();
						Date now7 =	TimeUtils.addDay(now, 7); //加7天时间 
						//更新需要退款的记录为删除状态时失败!
						mikuReturnGoodsDO.setStatus(Constants.ReturnGoodsStatus.DEL.getStatusId());	
						if(mikuReturnGoodsDOMapper.updateByPrimaryKeySelective(mikuReturnGoodsDO) < 0){
							log.info("更新退款表的记录为删除状态时失败!");
							transactionStatus.setRollbackOnly();
							welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
						}
						
						long consigneeId = -1l;
				        long communityId = -1l;
				        long logisticsId = -1l;
				        boolean isAgency = false;	//当购买者是代理商时（ture=是；false=不是代理商）
				        Long consigneeIdPre = null == mikuReturnGoodsDO.getConsigneeId() ? -1L : mikuReturnGoodsDO.getConsigneeId();
						Byte returnStatus = Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId();
				        if(communityId < 0){
							//如果异常状态无物流id,说明为未发货的order直接更新为退货中
				        	returnStatus = Constants.ReturnGoodsStatus.RETURNNING.getStatusId();	//退货中
						}else{
							//如果异常状态有物流id,说明已发货的order更新为申请退货
							returnStatus = Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId();	//申请退货
							
					        //寄货地址
							ConsigneeAddrDO consigneeAddrDO = addressService.fetchDefConsignee(mikuReturnGoodsDO.getBuyerId());
							if (consigneeAddrDO != null) {
					            communityId = consigneeAddrDO.getCommunityId();
					            logisticsId = addressService.addLogistics(consigneeAddrDO, -1L, (byte)1);
					            if (logisticsId < 0) {
					                welinkVO.setStatus(0);
					                welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
					                welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
					                return welinkVO;
					            }
					        } else {
					            welinkVO.setStatus(0);
					            welinkVO.setCode(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getCode());
					            welinkVO.setMsg(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getMsg());
					            return welinkVO;
					        }
						}
				        
				        mikuReturnGoodsDO.setId(null);
				        mikuReturnGoodsDO.setStatus(returnStatus);
				        mikuReturnGoodsDO.setConsigneeId(consigneeId);
				        mikuReturnGoodsDO.setTimeoutActionTime(now);
				        mikuReturnGoodsDO.setDateCreated(now);
				        mikuReturnGoodsDO.setLastUpdated(now);
				        mikuReturnGoodsDO.setBuyerMemo(buyerMemo);
				        //插入退货记录
						if(mikuReturnGoodsDOMapper.insertSelective(mikuReturnGoodsDO) < 1){
							log.info("更新退款表的记录为申请退款时失败!");
							transactionStatus.setRollbackOnly();
							welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
						}
						
						//更新miku_sales_record表状态为申请退货或退货中 
						/*MikuSalesRecordDOExample mikuSalesRecordDOExample = new MikuSalesRecordDOExample();
						mikuSalesRecordDOExample.createCriteria().andTradeIdEqualTo(mikuReturnGoodsDO.getTradeId())
							.andItemIdEqualTo(mikuReturnGoodsDO.getArtificialId());
							//.andReturnStatusEqualTo(Constants.ReturnGoodsStatus.NORMAL.getStatusId());	//正常分润记录
						MikuSalesRecordDO mikuSalesRecordDO = new MikuSalesRecordDO();
						mikuSalesRecordDO.setIsGetpay(Constants.GetPayStatus.NOGETPAY.getStatusId());		
						mikuSalesRecordDO.setReturnStatus(returnStatus);	//退货状态
						mikuSalesRecordDO.setTimeoutActionTime(now7);
						if(mikuSalesRecordDOMapper.updateByPrimaryKeySelective(mikuSalesRecordDO) < 0){
							log.info("更新分润记录表退货状态时失败");
							transactionStatus.setRollbackOnly();
							welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
						}*/
				        
				        /*MikuReturnGoodsDOExample mikuReturnGoodsDOExample = new MikuReturnGoodsDOExample();
				        mikuReturnGoodsDOExample.createCriteria().andTradeIdEqualTo(mikuReturnGoodsDO.getTradeId())
				        	.andArtificialIdEqualTo(mikuReturnGoodsDO.getArtificialId())
				        	.andStatusNotEqualTo(Constants.ReturnGoodsStatus.DEL.getStatusId());
				        MikuReturnGoodsDO mikuReturnGoodsDOupdate = new MikuReturnGoodsDO();
				        mikuReturnGoodsDOupdate.setStatus(returnStatus);
				        //更新代理的退款状态
				        mikuReturnGoodsDOMapper.updateByExampleSelective(mikuReturnGoodsDOupdate, mikuReturnGoodsDOExample);*/
				        
						//更新t_order表的状态为申请退货 orderMapper
				        Order order = new Order();
						order.setReturnStatus(returnStatus);	//退货状态
						if(orderMapper.updateByPrimaryKeySelective(order) != 1){
							log.info("更新订单退款状态为申请退款时失败");
							transactionStatus.setRollbackOnly();
							welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
						}
						
						return welinkVO;
					}
				});
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
	public WelinkVO expressReturnGood(final MikuReturnGoodsDO mikuReturnGoodsDO,
			final String expressCompany, final String expressNo) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						welinkVO.setStatus(1);
						Date nowDate = new Date();
						if(null != mikuReturnGoodsDO 
								&& !mikuReturnGoodsDO.getStatus().equals(Constants.ReturnGoodsStatus.REQ_EXAMINE.getStatusId())){
							welinkVO.setStatus(0);
							welinkVO.setMsg("啊哦~需审核通过才能退货");
							return welinkVO;
						}
						
						MikuReturnGoodsDOExample mikuReturnGoodsDOExample = new MikuReturnGoodsDOExample();
				        mikuReturnGoodsDOExample.createCriteria().andTradeIdEqualTo(mikuReturnGoodsDO.getTradeId())
				        	.andArtificialIdEqualTo(mikuReturnGoodsDO.getArtificialId())
				        	.andStatusNotEqualTo(Constants.ReturnGoodsStatus.DEL.getStatusId());
				        MikuReturnGoodsDO mikuReturnGoodsDOupdate = new MikuReturnGoodsDO();
				        //更新代理的退款状态
				        mikuReturnGoodsDOupdate.setStatus(Constants.ReturnGoodsStatus.RETURNNING.getStatusId());	//退货中
				        mikuReturnGoodsDOupdate.setConsignTime(nowDate);
				        //更新退款表的记录为退款中
						if(mikuReturnGoodsDOMapper.updateByExampleSelective(mikuReturnGoodsDOupdate, mikuReturnGoodsDOExample) < 1){
							log.info("更新退款表的记录为退款中时失败!");
							transactionStatus.setRollbackOnly();
							welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
						}
						if(null != mikuReturnGoodsDO.getConsigneeId()){
							LogisticsDO logisticsDO = logisticsDOMapper.selectByPrimaryKey(mikuReturnGoodsDO.getConsigneeId());
							if(null != logisticsDO){
								logisticsDO.setExpressCompany(expressCompany);
								logisticsDO.setExpressNo(expressNo);
								logisticsDO.setType((byte)1);
								if(logisticsDOMapper.updateByPrimaryKey(logisticsDO) != 1){
									log.info("更新物流信息的退款状态为退款中时失败!");
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
									welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
									return welinkVO;
								}
							}
						}
						//更新t_order表的状态为申请退货 orderMapper
						if(null != mikuReturnGoodsDO.getOrderId()){
							Order order = orderMapper.selectByPrimaryKey(mikuReturnGoodsDO.getOrderId());
							order.setReturnStatus(Constants.ReturnGoodsStatus.RETURNNING.getStatusId());	//退货中
							if(orderMapper.updateByPrimaryKeySelective(order) != 1){
								log.info("更新订单退款状态为退款中时失败");
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
								welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
								return welinkVO;
							}
							//更新miku_sales_record表状态为申请退货 mikuSalesRecordDOMapper
							MikuSalesRecordDOExample mikuSalesRecordDOExample = new MikuSalesRecordDOExample();
							mikuSalesRecordDOExample.createCriteria().andTradeIdEqualTo(order.getTradeId())
								.andItemIdEqualTo(order.getArtificialId());
								//.andReturnStatusNotEqualTo(Constants.ReturnGoodsStatus.REQ_RETURN.getStatusId());	//退款分润记录
							MikuSalesRecordDO mikuSalesRecordDO = new MikuSalesRecordDO();
							mikuSalesRecordDO.setReturnStatus(Constants.ReturnGoodsStatus.RETURNNING.getStatusId());	//退货中
							if(mikuSalesRecordDOMapper.updateByExampleSelective(mikuSalesRecordDO, mikuSalesRecordDOExample) < 0){
								log.info("更新分润记录表退款状态为退款中时失败");
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
								welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
								return welinkVO;
							}
						}
						return welinkVO;
					}

				});
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {

		//checkNotNull(mikuGetpayDOMapper);
		checkNotNull(transactionManager);
		
		transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setName("userAgentcy-transaction");
		transactionTemplate
				.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate
				.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
		//transactionTemplate.setTimeout(3000);
	}

	
}

