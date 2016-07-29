package com.welink.web.search.index.item;

import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.aliyun.opensearch.CloudsearchClient;
import com.aliyun.opensearch.CloudsearchDoc;
import com.google.common.collect.Maps;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("test")
public class ItemIndexServiceImplTest {

    @Resource
    private CloudsearchClient cloudsearchClient;

    @Resource
    private ItemIndexService itemIndexService;

    @Test
    public void testRefreshAll() throws Exception {
        itemIndexService.refreshAll();
    }

    @Test
    public void testInit() throws Exception {
        for (long i = 3000; i > 0; i--) {
            CloudsearchDoc cloudsearchDoc = new CloudsearchDoc("welinjia_search_test", cloudsearchClient);

            Map<String, Object> params = Maps.newHashMap();
            params.put("id", i);
            cloudsearchDoc.remove(params);
            String search_item = cloudsearchDoc.push("search_item");

            System.err.println("-----> " + JSON.toJSONString(JSON.parseObject(search_item), true));


            Thread.sleep(200);
        }
    }
    
}