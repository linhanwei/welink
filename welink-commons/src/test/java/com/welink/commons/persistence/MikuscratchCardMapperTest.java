/**
 * Project Name:welink-commons
 * File Name:TradeMapper.java
 * Package Name:com.welink.commons.persistence
 * Date:2016年1月12日上午9:17:44
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.persistence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.MikuActivityBonusDO;
import com.welink.commons.domain.MikuActivityBonusDOExample;
import com.welink.commons.domain.MikuCrowdfundDO;
import com.welink.commons.domain.MikuCrowdfundDOExample;
import com.welink.commons.domain.MikuScratchCardDO;
import com.welink.commons.domain.MikuScratchCardDOExample;
import com.welink.commons.domain.MikuCrowdfundDOExample.Criteria;
import com.welink.commons.utils.Utils;

/**
 * ClassName:TradeMapper <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月12日 上午9:17:44 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commons-applicationContext.xml"})
@ActiveProfiles("test")
public class MikuscratchCardMapperTest {
	@Resource
    private MikuScratchCardDOMapper mikuScratchCardDOMapper;
	
	@Resource
    private MikuActivityBonusDOMapper mikuActivityBonusDOMapper;
	
	@Resource
    private  ItemMapper itemMapper;
	
	@Test
	public void countScratchCard(){
		long profileId = 78770L;
		MikuScratchCardDOExample mikuScratchCardDOExample = new MikuScratchCardDOExample();
		mikuScratchCardDOExample.createCriteria().andUserIdEqualTo(profileId)
			.andDateCreatedGreaterThan(getTimesmorning());
		int countScratchCard = mikuScratchCardDOMapper.countByExample(mikuScratchCardDOExample);
		System.out.println("-----------------------------------------------------countScratchCard--: "+countScratchCard);
	}
	
	@Test
	public void mikuScratchCard(){
		long profileId = 78887L;
		int maxScratchCard = 1000;
		String probability = "0.083";
		/*if (env.isProd()) {
			maxScratchCard = 10;
			probability = "0.09";
        } else {
        	maxScratchCard = 5000;
        	probability = "0.95";
        }*/
		/*MikuScratchCardDOExample mikuScratchCardDOExample = new MikuScratchCardDOExample();
		mikuScratchCardDOExample.createCriteria().andUserIdEqualTo(profileId)
			.andDateCreatedGreaterThan(getTimesmorning());
		int countScratchCard = mikuScratchCardDOMapper.countByExample(mikuScratchCardDOExample);
		if(countScratchCard > maxScratchCard){	//每天最多刮5张
			welinkVO.setStatus(0);
			welinkVO.setCode(6);
			welinkVO.setMsg("亲~每天最多刮"+maxScratchCard+"张喲~");
			return JSON.toJSONString(welinkVO);
		}*/
		
		
		MikuActivityBonusDOExample mikuActivityBonusDOExample = new MikuActivityBonusDOExample();
		//mikuActivityBonusDOExample.createCriteria().andNumberEqualTo(number);
		mikuActivityBonusDOExample.setLimit(100);
		mikuActivityBonusDOExample.setOffset(100);
		List<MikuActivityBonusDO> mikuActivityBonusDOList = mikuActivityBonusDOMapper.selectByExample(mikuActivityBonusDOExample);
		int i = 0;
		int j = 0;
		for(MikuActivityBonusDO mikuActivityBonusDO : mikuActivityBonusDOList){
			i++;
			String number = mikuActivityBonusDO.getNumber();
			System.out.println("-------------------------------------------------------------number-:"+number);
			Date now = new Date();
			boolean isReward = false;
			MikuScratchCardDO mikuScratchCardDO = null;
			if(Utils.lucky(probability)){
				ItemExample itemExample = new ItemExample();
				itemExample.createCriteria().andApproveStatusEqualTo((byte)1)
					.andBaseItemIdIsNotNull().andTypeEqualTo((byte)10);
				List<Item> itemList = itemMapper.selectByExample(itemExample);
				if(!itemList.isEmpty()){
					Item item = itemList.get(0);
					if(null != item && null != item.getNum() && item.getNum() > 0){	//如果中奖
						isReward = true;
						mikuScratchCardDO = new MikuScratchCardDO();
						mikuScratchCardDO.setUserId(profileId);
						mikuScratchCardDO.setNumber(number);
						mikuScratchCardDO.setIsReward((byte)1);		//0=未中奖；1=中奖
						mikuScratchCardDO.setItemId(item.getId());
						mikuScratchCardDO.setItemTitle(item.getTitle());
						if (StringUtils.isNoneBlank(item.getPicUrls())) {
							mikuScratchCardDO.setPicUrls(StringUtils.split(item.getPicUrls(), ';')[0]);
						}
						mikuScratchCardDO.setStatus((byte)1);;	//状态(0=已取消;1=未下单;2=已下单;3=已付款)
						mikuScratchCardDO.setVersion(1L);
						mikuScratchCardDO.setDateCreated(now);
						mikuScratchCardDO.setLastUpdated(now);
						j++;
						System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>已中奖> "+number);
					}
				}
			}
			if(!isReward){	//如果未中奖
				mikuScratchCardDO = new MikuScratchCardDO();
				mikuScratchCardDO.setUserId(profileId);
				mikuScratchCardDO.setNumber(number);
				mikuScratchCardDO.setIsReward((byte)0);		//0=未中奖；1=中奖
				mikuScratchCardDO.setStatus((byte)1);;	//状态(0=已取消;1=未下单;2=未付款;3=已付款)
				mikuScratchCardDO.setVersion(1L);
				mikuScratchCardDO.setDateCreated(now);
				mikuScratchCardDO.setLastUpdated(now);
			}
			if(null != mikuScratchCardDO){
				if(mikuScratchCardDOMapper.insertSelective(mikuScratchCardDO) < 1){
					
				}
			}
		}
		System.out.println("............................................................中了多少个....j: "+j);
		
	}
	
	private Date getTimesmorning(){  
        Calendar todayStart = Calendar.getInstance();  
        todayStart.set(Calendar.HOUR, 0);  
        todayStart.set(Calendar.MINUTE, 0);  
        todayStart.set(Calendar.SECOND, 0);  
        todayStart.set(Calendar.MILLISECOND, 0);  
        return todayStart.getTime();  
    }
	
}

