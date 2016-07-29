package com.welink.commons.tacker;

import com.welink.commons.domain.AlipayBackDO;
import com.welink.commons.domain.AlipayBackDOExample;
import com.welink.commons.persistence.AlipayBackDOMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commons-applicationContext.xml"})
@ActiveProfiles("dev")
public class KeenIOTrackerTest {

    @Resource
    private KeenIOTracker keenIOTracker;

    @Resource
    private AlipayBackDOMapper alipayBackDOMapper;

    @Test
    public void testTrack() throws Exception {

        AlipayBackDOExample alipayBackDOExample = new AlipayBackDOExample();
        alipayBackDOExample.setOrderByClause("id DESC");

        List<AlipayBackDO> alipayBackDOs = alipayBackDOMapper.selectByExample(alipayBackDOExample);
        for (AlipayBackDO alipayBackDO : alipayBackDOs)
            keenIOTracker.track("15958116226", "sandbox", alipayBackDO, null);
    }

}