package com.welink.biz.service;

import com.welink.biz.common.model.AlipayInfoModel;
import com.welink.commons.domain.ProfileCoopDO;
import com.welink.commons.domain.ProfileCoopDOExample;
import com.welink.commons.persistence.ProfileCoopDOMapper;
import org.apache.shiro.session.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by XUTIAN on 2015/2/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commons-applicationContext.xml"})
@ActiveProfiles("dev")
public class UserServiceTest {
    @Resource
    private ProfileCoopDOMapper profileCoopDOMapper;

    @Test
    public void testAddCoopInfo() throws Exception {

        ProfileCoopDOExample profileCoopDOExample = new ProfileCoopDOExample();


        List<ProfileCoopDO> profileCoopDOs = profileCoopDOMapper.selectByExample(profileCoopDOExample);


        AlipayInfoModel alipayInfoModel = new AlipayInfoModel();

        boolean addCoopInfo = addCoopInfo("4", alipayInfoModel, null);
        System.out.println("------> " + addCoopInfo);


    }

    public boolean addCoopInfo(String loginType, AlipayInfoModel alipayInfoModel, Session session) {
        ProfileCoopDO profileCoopDO = new ProfileCoopDO();
        profileCoopDO.setUserId("w0OfHezdMnDi3jN1Rnuk7KNTa9baoZq0n9spyslaJQHfulL4Az3FnTMM3Pt74IIC");
        profileCoopDO.setVersion(1l);
        profileCoopDO.setDateCreated(new Date());
        profileCoopDO.setLastUpdated(new Date());
        profileCoopDO.setType(Byte.valueOf(loginType));
        profileCoopDO.setStatus(1);

        if (profileCoopDOMapper.insertSelective(profileCoopDO) < 0) {
            return false;
        }
        return true;
    }

}
