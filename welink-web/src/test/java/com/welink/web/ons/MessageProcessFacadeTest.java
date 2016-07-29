package com.welink.web.ons;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.welink.web.ons.config.ONSTopic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("dev")
public class MessageProcessFacadeTest {

    static Logger logger = LoggerFactory.getLogger(MessageProcessFacadeTest.class);

    @Resource
    private MessageProcessFacade messageProcessFacade;

    @Test
    public void testSendMessage() throws Exception {

        logger.info("-------------------------");

        messageProcessFacade.sendMessage(ONSTopic.SYSTEM_SIGNAL_TEST.toString(), "system", null, "hello, system");

        Map<String, String> params = Maps.newHashMap();
        params.put("item_id", "2076");
        params.put("type", "update");

        String message = JSON.toJSONString(params);

        messageProcessFacade.sendMessage(ONSTopic.ITEM_UPDATE_TEST.toString(), "item", null, message);

        logger.info("+++++++++++++++++++++++++");

        Thread.sleep(30000L);
    }
}