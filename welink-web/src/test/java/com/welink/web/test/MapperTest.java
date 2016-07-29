package com.welink.web.test;


import com.welink.commons.persistence.AnnouceDOMapper;
import com.welink.commons.persistence.BuildingDOMapper;
import com.welink.commons.persistence.ProfileExtDOMapper;
import com.welink.biz.service.AnnouceService;
import com.welink.commons.persistence.ItemMapper;
import com.welink.buy.service.AppointmentTradeService;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.notNullValue;


/**
 * Created by daniel on 14-9-17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("dev")
public class MapperTest {
    @Resource
    private BuildingDOMapper buildingDOMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private AnnouceDOMapper annouceDOMapper;

    @Resource
    private AnnouceService annouceService;

    @Resource
    private ProfileExtDOMapper profileExtDOMapper;

    @Resource
    private AppointmentTradeService appointmentService;

    @Before
    public void setup() {
        MatcherAssert.assertThat(buildingDOMapper, notNullValue());
    }

    @Test
    public void testAccuracy() {

        MatcherAssert.assertThat(buildingDOMapper, notNullValue());
        MatcherAssert.assertThat(annouceService, notNullValue());
        MatcherAssert.assertThat(profileExtDOMapper, notNullValue());
    }
}
