package com.welink.web.event;

import com.google.common.eventbus.AsyncEventBus;
import com.welink.commons.domain.AlipayBackDO;
import com.welink.commons.domain.AlipayBackDOExample;
import com.welink.commons.domain.WeiXinBackDO;
import com.welink.commons.domain.WeiXinBackDOExample;
import com.welink.commons.events.AlipayCallbackEvent;
import com.welink.commons.events.WechatCallbackEvent;
import com.welink.commons.persistence.AlipayBackDOMapper;
import com.welink.commons.persistence.WeiXinBackDOMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("dev")
public class PayCallbackEventHandlerTest {

    @Resource
    private AsyncEventBus asyncEventBus;

    @Resource
    private AlipayBackDOMapper alipayBackDOMapper;

    @Resource
    private WeiXinBackDOMapper weiXinBackDOMapper;


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testHandle() throws Exception {
        AlipayBackDOExample alipayBackDOExample = new AlipayBackDOExample();
        alipayBackDOExample.setOrderByClause("id DESC");

        List<AlipayBackDO> alipayBackDOs = alipayBackDOMapper.selectByExample(alipayBackDOExample);

        for (AlipayBackDO alipayBackDO : alipayBackDOs) {
            asyncEventBus.post(new AlipayCallbackEvent(alipayBackDO));
        }

        Thread.sleep(20000);
    }

    @Test
    public void testHandle2() throws Exception {
        WeiXinBackDOExample weiXinBackDOExample = new WeiXinBackDOExample();
        weiXinBackDOExample.setOrderByClause("id DESC");

        List<WeiXinBackDO> weiXinBackDOs = weiXinBackDOMapper.selectByExample(weiXinBackDOExample);

        for (WeiXinBackDO weiXinBackDO : weiXinBackDOs) {
            asyncEventBus.post(new WechatCallbackEvent(weiXinBackDO));
        }

        Thread.sleep(20000);
    }
}