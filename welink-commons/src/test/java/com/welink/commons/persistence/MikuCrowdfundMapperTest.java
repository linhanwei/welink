/**
 * Project Name:welink-commons
 * File Name:TradeMapper.java
 * Package Name:com.welink.commons.persistence
 * Date:2016年1月12日上午9:17:44
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.welink.commons.domain.MikuCrowdfundDO;
import com.welink.commons.domain.MikuCrowdfundDOExample;
import com.welink.commons.domain.MikuMineQuestionsDO;
import com.welink.commons.domain.MikuCrowdfundDOExample.Criteria;
import com.welink.commons.vo.MikuMineRecentlycontactLogVO;

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
public class MikuCrowdfundMapperTest {
	@Resource
    private MikuCrowdfundDOMapper mikuCrowdfundDOMapper;
	
	@Resource
    private MikuCsadDOMapper mikuCsadDOMapper;
	
	@Resource
    private MikuMineQuestionsDOMapper mikuMineQuestionsDOMapper;
	
	
	@Test
	public void getReturnGoodsVOList(){
		MikuCrowdfundDOExample mikuCrowdfundDOExample = new MikuCrowdfundDOExample();
		Criteria createCriteria = mikuCrowdfundDOExample.createCriteria();
		//createCriteria.andStatusEqualTo(Constants.CrowdfundStatus.NORMAL.getStatusId());
		createCriteria.andApproveStatusEqualTo((byte)1);
		createCriteria.andStatusEqualTo((byte)0);
 		mikuCrowdfundDOExample.setOrderByClause("weight asc");
 		mikuCrowdfundDOExample.setOffset(0);
 		mikuCrowdfundDOExample.setLimit(100);
 		List<MikuCrowdfundDO> mikuCrowdfundDOList = mikuCrowdfundDOMapper.selectByExample(mikuCrowdfundDOExample);
 		System.out.println("--------------------------------------------------------------------------------");
 		for(MikuCrowdfundDO mikuCrowdfundDO : mikuCrowdfundDOList){
 			System.out.println("Title........"+mikuCrowdfundDO.getTitle()+".......Weight:"+mikuCrowdfundDO.getWeight());
 		}
	}
	
	@Test
	public void getMineRecentlycontactLogVOList(){
		Integer type = 1;
		long profileId = 78887;
		List<MikuMineRecentlycontactLogVO> mikuMineRecentlycontactLogVOList = null;
	    Map<String, Object> paramMap = new HashMap<String, Object>();
	    if(null != type && type == 0){
	    	paramMap.put("userId", profileId);
	    }else{
	    	paramMap.put("csadUserId", profileId);
	    }
	    paramMap.put("orderByClause", "rl.last_updated DESC");
	    paramMap.put("limit", 7);
	    paramMap.put("offset", 0);
	    mikuMineRecentlycontactLogVOList = mikuCsadDOMapper.getMineRecentlycontactLogVOList(paramMap);
	    System.out.println("------------------------------------------size-"+mikuMineRecentlycontactLogVOList.size());
	    for(MikuMineRecentlycontactLogVO vo : mikuMineRecentlycontactLogVOList){
	    	System.out.println("CsadName: "+vo.getCsadName());
	    }
	}
	
	@Test
	public void selectQuestionByids(){
		String ids = "1,2,3";
		
		Map map=new HashMap<>();
    	map.put("ids", ids);
    	List<MikuMineQuestionsDO> list=mikuMineQuestionsDOMapper.selectQuestionByids(map);
    	System.out.println("----------------------------------------------list:"+list.size());
	}
	
}

