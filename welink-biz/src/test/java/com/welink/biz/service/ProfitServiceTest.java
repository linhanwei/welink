/**
 * Project Name:welink-biz
 * File Name:ProfitServiceTest.java
 * Package Name:com.welink.biz.service
 * Date:2015年12月17日下午5:43:06
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.biz.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.welink.biz.profit.AddProfitImpl;
import com.welink.commons.domain.MikuUserAgencyDO;
import com.welink.commons.domain.MikuUserAgencyDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.InstallActiveDOMapper;
import com.welink.commons.persistence.MikuUserAgencyDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;

/**
 * ClassName:ProfitServiceTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月17日 下午5:43:06 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-biz.xml"})
@ActiveProfiles("test")
public class ProfitServiceTest {

	@Resource
	private ProfitService profitService;
	
	@Resource
    private TradeService tradeService;
	
	@Resource
    private TradeMapper tradeMapper;
	
	@Resource
	private ItemService itemService;
	
	@Resource
	private AddProfitImpl addProfitImpl;
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;
	
	@Resource
	private InstallActiveDOMapper installActiveDOMapper;
	
	@Test
	public void insertMikuUserAgency(){
		profitService.profitByTradeId(6345034328523539L);
	}
	
	@Test
	public void AddProfitImpl(){
		List<Byte> status = new ArrayList<Byte>();
		status.add((byte)1);
		status.add((byte)2);
		status.add((byte)8);
		status.add((byte)9);
		/*TradeExample tradeExample = new TradeExample();
		tradeExample.createCriteria().andIsProfitEqualTo((byte)0)
			.andPUserIdGreaterThan(0L)
			.andStatusNotIn(status);
		tradeExample.setOffset(0);
		tradeExample.setLimit(36);
		List<Trade> tradeList = tradeMapper.selectByExample(tradeExample);
		for(Trade trade : tradeList){
			System.out.println(".............................TradeId:"+trade.getTradeId());
			addProfitImpl.handle(trade.getTradeId());
		}*/
		addProfitImpl.handle(1445757556501983L);
	}
	
	@Test
	public void cancelOrder(){
		TradeExample tradeExample = new TradeExample();
		tradeExample.createCriteria().andTradeIdEqualTo(324558764904602L);
		List<Trade> tradeList = tradeMapper.selectByExample(tradeExample);
		tradeService.cancelOrder(tradeList.get(0));
	}
	
	@Test
	public void test(){
		
		TradeExample tradeExample = new TradeExample();
        tradeExample.createCriteria().andTradeIdEqualTo(4245799845602245L);
        List<Trade> trades = tradeMapper.selectByExample(tradeExample);
        Trade trade = trades.get(0);
        Long profileId = trade.getBuyerId();	//交易购买者
		
		MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
    	mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(profileId);
    	//查询代理关系
    	List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample);	
    	
    	Map<Integer, Long> profileIdsMap = new HashMap<Integer, Long>();		//三级分润代理id对应用户id Map
    	MikuUserAgencyDO mikuUserAgencyDO = null;
    	boolean isShareItem = false;	//是否分享商品(true=是分享；false=不是分享)
    	if(!mikuUserAgencyDOList.isEmpty()){
    		mikuUserAgencyDO = mikuUserAgencyDOList.get(0);
    	}else if(mikuUserAgencyDOList.isEmpty() && !trade.getBuyerId().equals(trade.getpUserId())){
    		//mikuUserAgencyDO = new MikuUserAgencyDO();
    		if(null != trade.getpUserId() && trade.getpUserId() > 0){
    			MikuUserAgencyDOExample mikuUserAgencyDOExampleTradePid = new MikuUserAgencyDOExample();
    			mikuUserAgencyDOExampleTradePid.createCriteria().andUserIdEqualTo(trade.getpUserId());
    			//查询代理关系
	        	List<MikuUserAgencyDO> mikuUserAgencyDOTradePidList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExampleTradePid);	
	        	if(!mikuUserAgencyDOTradePidList.isEmpty()){
	        		ProfileDO tradePidProfileDO = profileDOMapper.selectByPrimaryKey(mikuUserAgencyDOTradePidList.get(0).getUserId());
	        		if(null != tradePidProfileDO && tradePidProfileDO.getIsAgency().equals((byte)1)){
	        			//判断推荐人是否代理,是代理按推荐人的代理关系分润
	        			mikuUserAgencyDO = mikuUserAgencyDOTradePidList.get(0);
	        			isShareItem = true;	//是否分享商品(true=是分享；false=不是分享)
	        		}
	        	}
    		}
    	}
    	if(null != mikuUserAgencyDO){
    		System.out.println("1........."+mikuUserAgencyDO.getUserId());
    		System.out.println("2........."+mikuUserAgencyDO.getpUserId());
    		System.out.println("3........."+mikuUserAgencyDO.getP2UserId());
    		System.out.println("4........."+mikuUserAgencyDO.getP3UserId());
    		System.out.println("5........."+mikuUserAgencyDO.getP4UserId());
    		System.out.println("6........."+mikuUserAgencyDO.getP5UserId());
    		System.out.println("7........."+mikuUserAgencyDO.getP6UserId());
    	}
	}
	
}

