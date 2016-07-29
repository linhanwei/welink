package com.welink.biz.persistence;

import com.welink.commons.domain.BuildingDO;
import com.welink.commons.persistence.*;
import com.welink.buy.service.AppointmentTradeService;
import junit.framework.Assert;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-biz.xml"})
@ActiveProfiles("dev")
public class BuildingDOMapperTest {

    @Resource
    private BuildingDOMapper buildingDOMapper;

    @Resource
    private TenantDOMapper tenantDOMapper;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private AnnouceDOMapper annouceDOMapper;

    @Resource
    private PropertyManagementFeesBillMapper pmfDOMapper;

    @Resource
    private ProfileExtDOMapper profileExtDOMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private AppointmentTradeService appointmentTradeService;

    @Before
    public void setup() {
        MatcherAssert.assertThat(buildingDOMapper, notNullValue());
    }

    @Test
    public void testAccuracy() {
        //确保mapper被加载
        MatcherAssert.assertThat(appointmentTradeService, notNullValue());
        MatcherAssert.assertThat(itemMapper, notNullValue());
        MatcherAssert.assertThat(buildingDOMapper, notNullValue());
        MatcherAssert.assertThat(tenantDOMapper, notNullValue());
        MatcherAssert.assertThat(profileDOMapper, notNullValue());
        MatcherAssert.assertThat(annouceDOMapper, notNullValue());
        MatcherAssert.assertThat(pmfDOMapper, notNullValue());
        MatcherAssert.assertThat(profileExtDOMapper, notNullValue());
    }
}