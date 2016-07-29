/**
 * Project Name:welink-web
 * File Name:MikuAgencyShareAccountDOMapperTest.java
 * Package Name:com.welink.web.test
 * Date:2015年11月4日下午10:54:45
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.test;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.vo.MikuAgencyShareAccountVO;

/**
 * ClassName:MikuAgencyShareAccountDOMapperTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月4日 下午10:54:45 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("dev")
public class MikuAgencyShareAccountDOMapperTest {
	@Resource
    private MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
	
	@Test
	public void selectAllyByParam(){
		/*List<MikuAgencyShareAccountVO> MikuAgencyShareAccountVOList = mikuAgencyShareAccountDOMapper.selectAllyByParam(1, 69357L, null);
		JSON.toJSONString(MikuAgencyShareAccountVOList);*/
	}
}

