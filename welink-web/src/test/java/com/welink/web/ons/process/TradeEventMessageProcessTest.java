package com.welink.web.ons.process;

import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.TradeMapper;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("dev")
public class TradeEventMessageProcessTest {

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private TradeEventMessageProcess tradeEventMessageProcess;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testPointAcquiredByTradeSuccess() throws Exception {
        TradeExample tradeExample = new TradeExample();
        tradeExample.setOrderByClause("id DESC");

        tradeExample.createCriteria() //
                .andTradeIdEqualTo(9242599432799273L);

        List<Trade> trades = tradeMapper.selectByExample(tradeExample);

        for (Trade trade : trades) {
            MatcherAssert.assertThat(tradeEventMessageProcess.pointAcquiredByTradeSuccess(trade), is(true));
            MatcherAssert.assertThat(tradeEventMessageProcess.pointAcquiredByTradeSuccess(trade), is(false));
            MatcherAssert.assertThat(tradeEventMessageProcess.pointAcquiredByTradeSuccess(trade), is(false));
            MatcherAssert.assertThat(tradeEventMessageProcess.pointAcquiredByTradeSuccess(trade), is(false));
            MatcherAssert.assertThat(tradeEventMessageProcess.pointAcquiredByTradeSuccess(trade), is(false));
            MatcherAssert.assertThat(tradeEventMessageProcess.pointAcquiredByTradeSuccess(trade), is(false));
            MatcherAssert.assertThat(tradeEventMessageProcess.pointAcquiredByTradeSuccess(trade), is(false));
        }


    }

    @Test
    public void testSendPointsAfterTradeSuccess() {
        TradeExample tradeExample = new TradeExample();
        tradeExample.createCriteria() //
                .andTradeIdEqualTo(5442786569202845L);

        List<Trade> trades = tradeMapper.selectByExample(tradeExample);

        for (Trade trade : trades) {
            MatcherAssert.assertThat(tradeEventMessageProcess.pointAcquiredByTradeSuccess(trade), is(true));
        }
    }
}