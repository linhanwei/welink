/**
 * Project Name:welink-biz
 * File Name:TradeService.java
 * Package Name:com.welink.biz.service
 * Date:2016年1月26日下午7:54:16
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.biz.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.mysql.jdbc.Connection;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.MikuScratchCardDO;
import com.welink.commons.domain.MikuScratchCardDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.GrouponDOMapper;
import com.welink.commons.persistence.MikuScratchCardDOMapper;
import com.welink.commons.persistence.ObjectTaggedDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.TradeMapper;

/**
 * ClassName:TradeService <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月26日 下午7:54:16 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@Service
public class TradeService implements InitializingBean {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(TradeService.class);

	@Resource
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;
	
    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private UsePromotionService usePromotionService;

    @Resource
    private ItemService itemService;
    
    @Resource
    private GrouponDOMapper grouponDOMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private AppointmentTradeService appointmentService;
    
    @Resource
    private ObjectTaggedDOMapper objectTaggedDOMapper;
    
    @Resource
    private MikuScratchCardDOMapper mikuScratchCardDOMapper;
    
    /**
     * 
     * cancelOrder:(取消订单). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param trade
     */
    public boolean cancelOrder(final Trade trade2){
    	return transactionTemplate
				.execute(new TransactionCallback<Boolean>() {
					@Override
					public Boolean doInTransaction(
							TransactionStatus transactionStatus) {
						Trade trade = null;
						if(null != trade2){
							trade = tradeMapper.selectByPrimaryKey(trade2.getId());
						}else{
							return false;
						}
						if (null != trade && trade.getStatus() == Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId()) {
			                trade.setStatus(Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId());
			                trade.setVersion(trade.getVersion() + 1);
			                trade.setLastUpdated(new Date());
			                trade.setEndTime(new Date());
			                
			                TradeExample tradeExample = new TradeExample();
			                tradeExample.createCriteria().andTradeIdEqualTo(trade.getTradeId());
			                if (tradeMapper.updateByExampleSelective(trade, tradeExample) < 1) {
			                    log.error("cancel trade failed. tradeId:" + trade.getTradeId());
			                    transactionStatus.setRollbackOnly();
                				return false;
			                }
			                usePromotionService.changePromotionFrozenToUnUsed(trade.getTradeId(), trade.getBuyerId());
			                //删除购买记录
			                itemService.deleteBuyRecord(trade);
			                
			                List<Order> orderList = findOrdersByTradeId(trade.getTradeId());	//根据交易号查找订单列表
			                if(null != orderList && !orderList.isEmpty()){
			                	for(Order order : orderList){
			                		if(null != order){
			                			if(!appointmentService.updateStock2(trade, order, transactionStatus, 2)){		//更新库存(2=加库存)
			                				transactionStatus.setRollbackOnly();
			                				return false;
			                			}
			                		}
			                	}
			                }
			                if(null != trade2 && null != trade2.getBuyerId() && null != trade2.getType()
		                			&& trade2.getType().equals(Constants.TradeType.scratch_card.getTradeTypeId())){
		                		//如果是刮刮卡订单
		                		MikuScratchCardDO mikuScratchCardDO = null;
		                		MikuScratchCardDOExample mikuScratchCardDOExample = new MikuScratchCardDOExample();
		                		mikuScratchCardDOExample.createCriteria().andUserIdEqualTo(trade2.getBuyerId())
		                			.andTradeIdEqualTo(trade2.getTradeId());
		                		List<MikuScratchCardDO> mikuScratchCardDOList = mikuScratchCardDOMapper.selectByExample(mikuScratchCardDOExample);
		                    	if(!mikuScratchCardDOList.isEmpty()){
		                    		mikuScratchCardDO = mikuScratchCardDOList.get(0);
		                    	}
		                		if(null != mikuScratchCardDO){
		                    		mikuScratchCardDO.setStatus(Constants.ScratchCardStatus.NO_ORDER.getStatusId());	//如果取消，设置刮刮卡为未下单
		                    		mikuScratchCardDO.setLastUpdated(new Date());
		                    		if(mikuScratchCardDOMapper.updateByPrimaryKeySelective(mikuScratchCardDO) < 1){	//更新刮刮卡状态为未付款
		                    			transactionStatus.setRollbackOnly();
		                    			return false;
		                    		}
		                    	}
		                	}
			                return true;
			            } else if (trade.getStatus() == Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId()) {
			                itemService.deleteBuyRecord(trade);
			            }
						return true;
					}

				});
    }
    
    public List<Order> findOrdersByTradeId(Long tradeId){
    	checkNotNull(tradeId);
    	OrderExample orderExample = new OrderExample();
    	orderExample.createCriteria().andTradeIdEqualTo(tradeId).andArtificialIdGreaterThan(0L);
    	List<Order> orderList = orderMapper.selectByExample(orderExample);
    	return orderList;
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

