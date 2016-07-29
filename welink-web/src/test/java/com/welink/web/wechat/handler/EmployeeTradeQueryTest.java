package com.welink.web.wechat.handler;

import com.google.common.base.Optional;
import com.daniel.weixin.mp.api.WxMpConfigStorage;
import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.api.WxMpServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("dev")
public class EmployeeTradeQueryTest {

    static Logger logger = LoggerFactory.getLogger(EmployeeTradeQueryTest.class);

    @Resource
    private EmployeeTradeQuery employeeTradeQuery;

    @Resource
    private WxMpConfigStorage wxMpConfigStorage;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testAccuracy() {
        Optional<String> query = employeeTradeQuery.query("15958116226");

        System.out.println(query.get());
        assertThat(query.isPresent(), is(true));
    }
}