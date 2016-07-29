package com.welink.web.ons.process;

import com.welink.commons.domain.Item;
import com.welink.commons.persistence.ItemMapper;
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


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("dev")
public class ItemUpdateMessageProcessTest {

    static Logger logger = LoggerFactory.getLogger(ItemUpdateMessageProcess.class);

    @Resource
    private ItemUpdateMessageProcess itemUpdateMessageProcess;

    @Resource
    private ItemMapper itemMapper;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRefreshAllItems() throws Exception {
        Item item = itemMapper.selectByPrimaryKey(2871L);
        itemUpdateMessageProcess.updateSearchItem(item, "update");

        Thread.sleep(8880000);
    }
}