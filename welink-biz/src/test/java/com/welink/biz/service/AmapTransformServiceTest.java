package com.welink.biz.service;

import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-biz.xml"})
@ActiveProfiles("dev")
public class AmapTransformServiceTest {

    @Resource
    private AmapTransformService amapTransformService;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testPointTransform() throws Exception {

        Optional<String> stringOptional = amapTransformService.pointTransform("120.0169,30.24469");

        assertThat(stringOptional.isPresent(), is(true));

        System.out.println(stringOptional.get());
    }
}