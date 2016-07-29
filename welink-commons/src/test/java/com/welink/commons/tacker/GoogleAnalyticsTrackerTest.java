package com.welink.commons.tacker;

import com.welink.commons.domain.AlipayBackDO;
import com.welink.commons.persistence.AlipayBackDOMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commons-applicationContext.xml"})
@ActiveProfiles("dev")
public class GoogleAnalyticsTrackerTest {

    @Resource
    private AlipayBackDOMapper alipayBackDOMapper;

    @Resource
    private GoogleAnalyticsTracker googleAnalyticsTracker;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testTrack() throws Exception {

        AlipayBackDO alipayBackDO = alipayBackDOMapper.selectByPrimaryKey(1613L);

        int num = new Double(Double.valueOf(alipayBackDO.getTotalFee()) * 100D).intValue();
        if (num > 0) {
            googleAnalyticsTracker.track("alipay", alipayBackDO.getTradeStatus(), "15958116226", num);
        }
    }

    @Test
    public void testInit() throws Exception {

    }
}