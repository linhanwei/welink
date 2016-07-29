/**
 * Project Name:welink-biz
 * File Name:AddProfitImplTest.java
 * Package Name:com.welink.biz.service
 * Date:2016年3月8日上午11:56:43
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.biz.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.welink.biz.profit.AddProfitImpl;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.events.ProfitEvent;
import com.welink.commons.persistence.TradeMapper;

/**
 * ClassName:AddProfitImplTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年3月8日 上午11:56:43 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-biz.xml"})
@ActiveProfiles("test")
public class AddProfitImplTest {
	
	@Resource
	private AddProfitImpl addProfitImpl;
	
	@Resource
	private TradeMapper tradeMapper;
	
	@Test
	public void addfrofit(){
		//addProfitImpl.handle(1945749838012862L);
		List<Byte> toDealStatus = new ArrayList<>();	//可分润trade状态
		toDealStatus.add(Constants.TradeStatus.SELLER_CONSIGNED_PART.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
		TradeExample toShareFeeExample = new TradeExample();
		toShareFeeExample.createCriteria().andStatusIn(toDealStatus)
        	.andIsProfitEqualTo((byte)0);
        List<Trade> toShareFeeTrades = tradeMapper.selectByExample(toShareFeeExample);
        if(!toShareFeeTrades.isEmpty()){
        	addProfitImpl.handle(toShareFeeTrades.get(0).getTradeId());
        	System.out.println("----------------------------------------------");
        	System.out.println("==========================TradeId:"+toShareFeeTrades.get(0).getTradeId());
        }
        /*for(Trade trade : toShareFeeTrades){
        	if(null != trade && null != trade.getTradeId()){
        	}
        }*/
	}
}

