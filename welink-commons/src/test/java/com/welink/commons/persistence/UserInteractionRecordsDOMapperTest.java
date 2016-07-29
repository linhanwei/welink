package com.welink.commons.persistence;

import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.domain.UserInteractionRecordsDOExample;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
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

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commons-applicationContext.xml"})
@ActiveProfiles("test")
public class UserInteractionRecordsDOMapperTest {

    static Logger logger = LoggerFactory.getLogger(UserInteractionRecordsDOMapperTest.class);

    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;

    @Before
    public void setUp() throws Exception {
        MatcherAssert.assertThat(userInteractionRecordsDOMapper, notNullValue());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testAccuracy() throws Exception {
        UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
        userInteractionRecordsDO.setFrom(2);
        userInteractionRecordsDO.setUserId(9999L);
        MatcherAssert.assertThat(userInteractionRecordsDOMapper.insert(userInteractionRecordsDO), is(1));

        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.createCriteria().andFromEqualTo(2);
        MatcherAssert.assertThat(userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample).size(), is(1));

        MatcherAssert.assertThat(userInteractionRecordsDOMapper.selectByPrimaryKey(userInteractionRecordsDO.getId()), CoreMatchers.notNullValue());

        MatcherAssert.assertThat(userInteractionRecordsDOMapper.deleteByPrimaryKey(userInteractionRecordsDO.getId()), is(1));
    }
}