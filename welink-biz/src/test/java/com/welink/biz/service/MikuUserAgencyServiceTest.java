/**
 * Project Name:welink-biz
 * File Name:MikuUserAgencyServiceTest.java
 * Package Name:com.welink.biz.service
 * Date:2015年11月16日上午10:02:44
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.biz.service;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileWeChatDO;
import com.welink.commons.vo.LotteryDrawRewardVO;

/**
 * ClassName:MikuUserAgencyServiceTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月16日 上午10:02:44 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-biz.xml"})
@ActiveProfiles("test")
public class MikuUserAgencyServiceTest {

	@Resource
	private MikuUserAgencyService mikuUserAgencyService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private MikuGetPayService mikuGetPayService;
	
	@Resource
	private LotteryDrawInteractionService lotteryDrawInteractionService;
	
	@Test
	public void insertMikuUserAgency(){
		//mikuUserAgencyService.insertMikuUserAgency(-1L, 999L);
		ProfileDO profileDO = mikuUserAgencyService.addProfileAndAgency(-1L, "15622395287", null, (byte)0, "-1", Byte.valueOf("1"));
	}
	
	@Test
	public void lotteryDrawInteractionService(){
		System.out.println("------------------------------------------------------------------------------------");
		String lotteryDrawRewardVOStr = "";
		LotteryDrawRewardVO lotteryDrawRewardVO = new LotteryDrawRewardVO();
		//lotteryDrawRewardVO.setType(1);
		//lotteryDrawRewardVO.setIndex(1001);
		lotteryDrawRewardVO = lotteryDrawInteractionService.interactive(70468L);
		lotteryDrawRewardVOStr = JSON.toJSONString(lotteryDrawRewardVO);
		System.out.println("111111111111111111..."+lotteryDrawRewardVOStr);
		
		//lotteryDrawRewardVO = lotteryDrawInteractionService.interactive(69677L);
		
		/*lotteryDrawRewardVO.setType(2);
		lotteryDrawRewardVO.setIndex(2001);
		//lotteryDrawInteractionService.interactive(69677L);
		lotteryDrawRewardVOStr = JSON.toJSONString(lotteryDrawRewardVO);
		System.out.println("22222222222222222222222..."+lotteryDrawRewardVOStr);
		
		lotteryDrawRewardVO.setType(3);
		//lotteryDrawRewardVO.setIndex(3001);
		//lotteryDrawInteractionService.interactive(69677L);
		lotteryDrawRewardVOStr = JSON.toJSONString(lotteryDrawRewardVO);
		System.out.println("3333333333333333333333..."+lotteryDrawRewardVOStr);*/
		
		if(null == lotteryDrawRewardVO){
			lotteryDrawRewardVO = new LotteryDrawRewardVO();
			lotteryDrawRewardVO.setType(-1);
		}
		lotteryDrawRewardVOStr = JSON.toJSONString(lotteryDrawRewardVO);
		System.out.println("44444444444444444444444444..."+lotteryDrawRewardVOStr);
	}
	
	@Test
	public void reqGetPay(){
		System.out.println("------------------------------------------------------------------------------------");
		Integer getpayType = 2;
		String account = null, accountName="彭康";
		Long profileId = 79571L;
		ProfileWeChatDO profileWeChatDO = userService.getProfileWeChatByProfileId(profileId);
		if(null != profileWeChatDO){
			account = profileWeChatDO.getOpenid();
		}
		if(StringUtils.isNotBlank(account)){
			mikuGetPayService.reqGetPay(profileId, getpayType, account, accountName);
		}
	}
	
	@Test
	public void updateMikuUserAgency(){
		mikuUserAgencyService.updateMikuUserAgency(78887L, 72453L, null);
	}
	
	
}

